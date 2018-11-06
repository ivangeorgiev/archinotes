# NiFi API

[TOC]

API Endpoint is: http://localhost:8080/nifi-api/flow/

Read the root NiFi Flow: /nifi-api/flow/process-groups/root

* GET http://localhost:8011/nifi-api/flow/process-groups/root
  * The processGroupFlow.id is: 7f9398a2-0164-1000-2e5f-8d1a56e4a00d

* GET http://localhost:8011/nifi-api/flow/process-groups/7f9398a2-0164-1000-2e5f-8d1a56e4a00d
  * Result is ProcessGroupFlowEntity
  * procesGroupFlow.flow.connections contains connections inside the group





