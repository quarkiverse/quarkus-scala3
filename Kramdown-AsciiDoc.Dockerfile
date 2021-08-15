FROM ruby:3-alpine3.14
RUN gem install kramdown-asciidoc
ENTRYPOINT [ "kramdoc" ]