const lineByLine = require('n-readlines');

const INPUT_FILE = 'tmp/vdv.txt';

const START_AFTER = "alphabetisch nach Namen | inklusive Anschrift und Telefonnummer";
const END_BEFORE = "Kamekestraße 37—39 · 50672 Köln";
const SPECIAL_MEMBER_MARKER = "Außerordentliche Mitglieder";
const IGNORE = [
  "Verzeichnis: Mitglieder im VDV",
  /.*VDV-Statistik 2019.*/,
  "alphabetisch nach Namen | inklusive Anschrift und Telefonnummer"
];

// See https://www.vdv.de/sparten.aspx for explanaitions of each category
const CATEGORIES = [
  "BUS",
  "TRAM",
  "PVE",
  "SGV",
  "Verbünde/AT"
];

const PATTERN_STREET_NUMBER = /(\D* \d+)|(\d+ rue\D+)/;
const PATTERN_ZIP_CITY = /\d\d\d\d\d? \D+/;
const PATTERN_PHONE = /[\d- ]+/;

// handle special cases
const REPLACEMENTS = [
  ["0531 383-2050 . BUS TRAM", "0531 383-2050 .... BUS TRAM"], // single dot is not recognizable as separator
  ["06162 809 839-0 .", "06162 809 839-0 ...."], // single dot is not recognizable as separator
  ["030 247387-0", "030 247387-0 ...." ], // no categories present, but still needs separator
  ["0388 77 70 70", "+ 33 (0)3 88 77 70 70"], // "Compagnie des Transports Strasbourgeois" is missing international calling code
  ["5000 Odense C", "5000 Odense C | +45 63 75 74 73"] // needs phone number to be recognized as non-german
];

function processFile() {
  const liner = new lineByLine(INPUT_FILE);

  let line, buffer = "", special = false;

  console.log("Name;Street;ZIP & City;Phone;Country;Special Member;" + CATEGORIES.join(";"));

  // Ignore all lines before / including START_AFTER
  while (line = liner.next()) {
    const lineString = line.toString('UTF-8').trim();
    if (lineString == START_AFTER)
      break;
  }

  // Process all lines before END_BEFORE
  outer: while ((line = liner.next())) {
    let lineString = line.toString('UTF-8').trim();

    if (lineString == END_BEFORE)
      break;
    if (lineString == "")
      continue;
    if (lineString == SPECIAL_MEMBER_MARKER) {
      special = true;
      continue;
    }

    for (const pattern of IGNORE) {
      if (lineString == pattern || lineString.match(pattern)) {
        // console.log(lineString + " == " + pattern);
        continue outer;
      }
    }

    // Each entry may consist of mulitple lines, the last line will contain a long sequence of dots ('.')
    if (contains(lineString, " .") || contains(lineString, "....")) { // final line of sequence
      buffer += lineString;
      try {
        processEntry(buffer, special);
      } catch (e) {
        console.error(e.message);
        console.error("In line: " + buffer);
      }
      buffer = "";
    } else {  // non-final line of sequence
      buffer += lineString + " | ";
    }
  }
}

function processEntry(entry, special) {
  // Handle special cases
  for (const replacement of REPLACEMENTS) {
    entry = entry.replace(replacement[0], replacement[1]);
  }

  const outerParts = entry.split(/\.\.\.*/);

  // Parts are Name, Street, ZIP & City, Phone, Categories
  let parts = outerParts[0].split("|").map(s => s.trim()).filter(p => p.length > 0);

  const categories = outerParts[1].split(" ");
  const categoryBooleans = CATEGORIES.map(cat => categories.includes(cat));

  // Handle company names that were split with '|' because they were too long
  if (!parts[1].match(PATTERN_STREET_NUMBER) && parts[2].match(PATTERN_STREET_NUMBER)) {
    parts = [parts[0] + " " + parts[1], ...parts.slice(2)];
  }

  if (parts.length == 3 && parts[2].match(PATTERN_ZIP_CITY)) {
    parts = [...parts, ""];
  }

  if (parts.length != 4) {
    throw new Error("Not 4 parts, but " + parts.length + " in " + JSON.stringify(parts));
  }

  let country = "Deutschland";

  const phone = parts[3];

  if ((beginsWith(phone, "00") && !beginsWith(phone, "0049")) || (beginsWith(phone, "+") && !beginsWith(phone, "+49")))
    country = "?";

  console.log(parts.join(";") + ";" + country + ";" + special + ";" + categoryBooleans.join(";"));

}

function contains(s, c) {
  return s.indexOf(c) != -1;
}

function beginsWith(s, c) {
  return s.indexOf(c) == 0;
}

processFile();