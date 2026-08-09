# GeoCeDG feature manifests

`stable.yml` and `experimental.yml` are the durable feature-set manifests.
They use the JSON-compatible YAML profile defined by
`geocedg/specs/operations/manifest-contracts.md`.

G1 leaves both sets empty. Adding an entry does not by itself authorize an
implementation: the referenced specification and roadmap gate must already be
approved. Experimental entries require a feature flag and must not become
default merely by appearing in a toolbar or package.
