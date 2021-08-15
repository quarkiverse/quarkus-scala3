#!/bin/bash
kramdoc -o ./docs/modules/ROOT/pages/index.adoc README.md
# [[ -n "$(docker images -q kramdown-asciidoc:latest)" ]] || {
#   docker build -t kramdown-asciidoc:latest -f Kramdown-AsciiDoc.Dockerfile .
# }
# docker run -v $(pwd):/ -it kramdown-asciidoc:latest -o ./docs/modules/ROOT/pages/index.adoc README.md
echo "Converted. Dont forget to add ':extension-status: preview' underneath the first line!"
