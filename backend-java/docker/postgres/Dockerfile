FROM postgres:17-alpine
USER root
COPY docker/postgres/postgres-ssl-entrypoint.sh /usr/local/bin/postgres-ssl-entrypoint.sh
RUN chmod +x /usr/local/bin/postgres-ssl-entrypoint.sh
ENTRYPOINT ["/usr/local/bin/postgres-ssl-entrypoint.sh"]
CMD ["postgres"]
