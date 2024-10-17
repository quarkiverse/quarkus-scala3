# ZIO TODOs / IDEAs

- ZIO-Config: Create implementation of ZIO ConfigProvider that is backed by the microprofile Config and
  inject that into the environment / make it usable with ZIO.Config(..) [See ZIO-Configuration](https://zio.dev/reference/configuration/)
- ZIO-Logging: Bridge ZIO-Logging to Quarkus-Logger?
- Fill the environment with commonly required dependencies.
- Fill the environment with dependencies specified in the `R` type (we need to figure out how to parse that, as we loose type info in Java)
- Allow using `ZStream` where `Multi` is allowed atm (e.g. Kafka, SSE, etc.)
- Register the ZIO Runtime using a Provider, so it could be injected.
- ...
