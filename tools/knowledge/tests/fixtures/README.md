# G9O1 knowledge-bundle fixture

The focused test creates a disposable Git repository and combines this profile
with small native, upstream-modified, upstream-unchanged, generated and
restricted paths. The repository is built at runtime so that baseline/current
blob identities, clean/dirty state and freshness checks use real Git objects.
The suite also exercises configured exclusions, chunk planning, unsafe paths,
unsafe remote provenance and inventory disagreement without repository fixtures.

No generated fixture bundle is checked in.
