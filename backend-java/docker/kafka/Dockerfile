FROM apache/kafka:4.3.1
USER root
COPY docker/kafka/entrypoint.sh /opt/kafka/custom-entrypoint.sh
COPY docker/kafka/init-topics-acls.sh /opt/kafka/init-topics-acls.sh
RUN chmod +x /opt/kafka/custom-entrypoint.sh /opt/kafka/init-topics-acls.sh
ENTRYPOINT ["/opt/kafka/custom-entrypoint.sh"]
