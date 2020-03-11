const lineByLine = require('n-readlines');
const fs = require('fs');
const leven = require('leven');
const prompt = require('async-prompt');

const commandLineArgs = process.argv.slice(2);

const AGENCY_FILE = commandLineArgs[0];
const VDV_FILE = 'output/vdv.csv';
const MATCHES_FILE = 'output/matches.csv';

const vdv = [];
const agencies = [];
const already_matched = [];

function processVdvFile() {
    const liner = new lineByLine(VDV_FILE);
    let line;

    liner.next();

    while (line = liner.next()) {
        const lineString = line.toString('UTF-8').trim();
        const parts = lineString.split(";");
        vdv.push(parts[0]);
    }
}

function processAgenciesFile() {
    const liner = new lineByLine(AGENCY_FILE);
    let line;

    liner.next();

    while (line = liner.next()) {
        const lineString = line.toString('UTF-8').trim();
        const parts = lineString.split(",");
        agencies.push(parts[1]);
    }
}


function processMatchesFile() {
    const liner = new lineByLine(MATCHES_FILE);
    let line;

    liner.next();

    while (line = liner.next()) {
        const lineString = line.toString('UTF-8').trim();
        const parts = lineString.split(";");
        already_matched.push(parts[1]);
    }
}

function compareLists() {
    for (const v2 of agencies) {
        var bestMatchString = "";
        var bestMatchValue = 0;
        for (const v1 of vdv) {
            let match = 0;
            if (v1 == v2)
                match = 1;
            else if (contains(v1, v2))
                match = v2.length / v1.length;
            else if (contains(v2, v1))
                match = v1.length / v2.length;

            if (match < 0.3) {
                const refLen = Math.max(v1.length, v2.length);
                match = (refLen - leven(v1, v2)) / refLen;
            }

            if (match > bestMatchValue) {
                bestMatchString = v1;
                bestMatchValue = match;
            }
        }

        if (bestMatchValue > 0.6 && !already_matched.includes(v2)) {
            fs.appendFileSync(MATCHES_FILE, Math.floor(bestMatchValue * 100) + "%;" + v2 + ";" + bestMatchString +"\n");
        }
    }
}

async function processUnmatched() {
    for (const v of agencies) {
        if (already_matched.includes(v))
            continue;

        console.log("\n\nUnmatched: " + v);
        const search = await prompt('Search for:');
        const potentialMatches = vdv.filter(v => contains(v.toLowerCase(), search.toLowerCase()));
        if(search.length > 1 && potentialMatches.length > 0) {
            for (let i = 0; i < potentialMatches.length; i++) {
                const element = potentialMatches[i];
                console.log((i+1)+ ". " + element);
            }
            const choice = await prompt('Which one to use? (0 or empty to skip)');
            if(choice > 0) {
                fs.appendFileSync(MATCHES_FILE, "manual;"+v+";"+potentialMatches[choice-1]+"\n");
                console.log("Written to file.");
            }
        } else {
            console.log("No match for search.")
        }
    }
}

function contains(s, c) {
    return s.indexOf(c) != -1;
}

function beginsWith(s, c) {
    return s.indexOf(c) == 0;
}

if(!AGENCY_FILE) {
   console.error("You must supply the path to an agency.txt file as single parameter.");
   process.exit(1);
}

console.log("Reading souce files...");
processVdvFile();
processAgenciesFile();
processMatchesFile();

console.log("Performing automated matching...");
compareLists();

console.log("Performing manual matching...");
console.log("Each unmatched agency from the input file will be displayed. You may then input a search term (typically a signigicant substring of the agency name) to see potential matches from the vdv list, and select the matching one.");
processUnmatched();