# abort on errors
set -e

file=tmp/vdv.pdf 
uri=https://www.vdv.de/vdv-mitgliederverzeichnis.pdfx

# only download if changed since last download, of if not present at all
#if test -e "$file"
#then zflag="-z '$file'"
#else zflag=
#fi
#curl -o "$file" $zflag "$uri"

pdf2txt.py -o tmp/vdv.txt tmp/vdv.pdf

npm install

node convert_vdv.js > output/vdv.csv