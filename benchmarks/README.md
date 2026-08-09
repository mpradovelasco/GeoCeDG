# GeoCeDG benchmark support

Benchmark suites are repository-owned descriptors executed by
`tools/benchmark/run.ps1`. G1 establishes the harness and one operational
smoke suite; it does not claim a kernel or rendering performance baseline.

Budgets are initially informational. Command failures are errors, while a
timing threshold produces an `informational-exceeded` result and does not fail
the harness. Each result records the commit, environment, warm-up count,
measurement count, individual durations, and summary statistics.

`models/stress-catalog.yml` contains disabled planning descriptors only. Model
assets and geometric expected results require an approved specification and
are not imported during G1.

Generated results belong under `artifacts/benchmarks/` and are ignored by Git.
