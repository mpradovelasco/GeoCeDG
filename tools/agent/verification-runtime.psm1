#requires -Version 7.2
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Receipt authority is confined to one module instance and one live invocation.
$script:ActiveEvidence = $null
$script:ModuleIdentity = [guid]::NewGuid().ToString("N")
$script:Lf = [string][char]10
$script:MandatoryUpstreamClasses = @(
    "org.geogebra.common.kernel.commands.RedefineTest",
    "org.geogebra.common.euclidian.DrawablesTest",
    "org.geogebra.common.kernel.LocusV2InputPreviewLifecycleTest"
)
$script:ModuleDefinitions = [ordered]@{
    shared = [ordered]@{
        Task = ":shared:common-jre:test"
        ResultDirectory = "source/shared/common-jre/build/test-results/test"
        Styles = [ordered]@{
            ":shared:common:checkstyleMain" = "source/shared/common/build/reports/checkstyle/main.xml"
            ":shared:common-jre:checkstyleTest" = "source/shared/common-jre/build/reports/checkstyle/test.xml"
        }
    }
    desktop = [ordered]@{
        Task = ":desktop:desktop:test"
        ResultDirectory = "source/desktop/desktop/build/test-results/test"
        Styles = [ordered]@{
            ":desktop:desktop:checkstyleMain" = "source/desktop/desktop/build/reports/checkstyle/main.xml"
            ":desktop:desktop:checkstyleTest" = "source/desktop/desktop/build/reports/checkstyle/test.xml"
        }
    }
}
$script:PhaseVerifiers = [ordered]@{
    G2 = "verify-frontend.ps1"
    G5 = "verify-dxf.ps1"
    G6 = "verify-locus-v2.ps1"
    G7A = "verify-g7a-metrics.ps1"
    G7B = "verify-g7b-metrics.ps1"
    G8A = "verify-g8a-intersections.ps1"
    G8B = "verify-g8b-intersections.ps1"
    G8C = "verify-g8c-intersections-design.ps1"
    G8C1 = "verify-g8c1-intersections.ps1"
    G8C2 = "verify-g8c2-intersections.ps1"
    G9A1 = "verify-g9a1-spatial-identity.ps1"
    G9A2 = "verify-g9a2-spatial-point.ps1"
    G9A3 = "verify-g9a3-spatial-lifecycle.ps1"
    G9U0 = "verify-g9u0-locus-v2-public-surface.ps1"
    "G9U0-R1" = "verify-g9u0-r1-locus-v2-public-creation-lifecycle.ps1"
    G9X1 = "verify-g9x1-extended-dxf.ps1"
    "G9U0-R2" = "verify-g9u0-r2-product-refinement.ps1"
    "G9U0-R3" = "verify-g9u0-r3-public-locus-ui-hardening.ps1"
    "G9U0-R4" = "verify-g9u0-r4-intersection-admissibility-continuation.ps1"
    "G9U0-R5" = "verify-g9u0-r5-locus-v2-similarity-transformations.ps1"
    G9S1 = "verify-g9s1-semantic-spline-2d-capability.ps1"
    "G9U0-R6" = "verify-g9u0-r6-semantic-locus-point-interaction-support.ps1"
}

function Get-TextSha256 {
    param([Parameter(Mandatory)] [AllowEmptyString()] [string]$Text)
    $bytes = [Text.Encoding]::UTF8.GetBytes($Text)
    $algorithm = [Security.Cryptography.SHA256]::Create()
    try {
        return [BitConverter]::ToString($algorithm.ComputeHash($bytes)).Replace("-", "").ToLowerInvariant()
    } finally {
        $algorithm.Dispose()
    }
}

function Get-RawFileSha256 {
    param([Parameter(Mandatory)] [string]$Path)
    $stream = [IO.File]::Open($Path, [IO.FileMode]::Open, [IO.FileAccess]::Read, [IO.FileShare]::Read)
    $algorithm = [Security.Cryptography.SHA256]::Create()
    try {
        return [BitConverter]::ToString($algorithm.ComputeHash($stream)).Replace("-", "").ToLowerInvariant()
    } finally {
        $algorithm.Dispose()
        $stream.Dispose()
    }
}

$script:LoadedRuntimePath = [IO.Path]::GetFullPath($PSCommandPath)
$script:LoadedRuntimeSha256 = Get-RawFileSha256 -Path $script:LoadedRuntimePath

function Assert-LoadedRuntimeSource {
    $reloadInstruction = "Start a fresh PowerShell session, or explicitly reload this module only after all verification runs have closed."
    try {
        $currentHash = Get-RawFileSha256 -Path $script:LoadedRuntimePath
    } catch {
        throw "Cannot read loaded verification runtime source: $($script:LoadedRuntimePath). $reloadInstruction"
    }
    if ($currentHash -cne $script:LoadedRuntimeSha256) {
        throw "Loaded verification runtime is stale relative to disk: $($script:LoadedRuntimePath). $reloadInstruction"
    }
}

function Write-VerificationJson {
    param([Parameter(Mandatory)] [string]$Path, [Parameter(Mandatory)] [object]$Value)
    [void][IO.Directory]::CreateDirectory((Split-Path -Parent $Path))
    $json = ($Value | ConvertTo-Json -Depth 100).Replace(([string][char]13 + [char]10), $script:Lf)
    [IO.File]::WriteAllText($Path, $json + $script:Lf, [Text.UTF8Encoding]::new($false))
}

function Resolve-VerificationChildPath {
    param([Parameter(Mandatory)] [string]$Root, [Parameter(Mandatory)] [string]$RelativePath)
    if ([IO.Path]::IsPathRooted($RelativePath)) {
        throw "Expected a relative verification path: $RelativePath"
    }
    $rootPath = [IO.Path]::GetFullPath($Root).TrimEnd("/", "\")
    $absolute = [IO.Path]::GetFullPath((Join-Path $rootPath $RelativePath))
    if (-not $absolute.StartsWith($rootPath + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Verification path escapes its root: $RelativePath"
    }
    return $absolute
}

function Invoke-VerificationGit {
    param([Parameter(Mandatory)] [string]$RepositoryRoot, [Parameter(Mandatory)] [string[]]$Arguments)
    $PSNativeCommandUseErrorActionPreference = $false
    $global:LASTEXITCODE = $null
    $output = @(& git --no-optional-locks -c core.quotepath=false -C $RepositoryRoot @Arguments 2>&1 | ForEach-Object { $_.ToString() })
    $code = $global:LASTEXITCODE
    if ($null -eq $code) { throw "Verification input inventory: git returned no native exit code." }
    if ($code -ne 0) {
        throw "Verification input inventory: git exited $code. $($output -join ' ')"
    }
    return $output
}

function Get-RawInputInventory {
    param([Parameter(Mandatory)] [string]$RepositoryRoot)
    # Git visibility is not the same as Gradle input visibility. An unchanged
    # global/info ignore rule must not hide a newly added consumed source file.
    # Collapse wholly ignored directories before traversal. In particular, book
    # is an opaque, independent repository link, not a tree to descend or hash.
    $ignoredPaths = @(Invoke-VerificationGit -RepositoryRoot $RepositoryRoot -Arguments @(
        "ls-files", "--others", "--ignored", "--exclude-standard", "--directory"
    ))
    $unexpectedIgnored = [Collections.Generic.List[string]]::new()
    foreach ($relative in $ignoredPaths) {
        # Git emits '/' separators and a final '/' for a collapsed directory.
        # Preserve that marker: an opaque ignored file named build is not a
        # generated directory. Reject warnings/aliases before classifying paths.
        $isDirectory = $relative.EndsWith("/", [StringComparison]::Ordinal)
        $withoutMarker = if ($isDirectory) { $relative.Substring(0, $relative.Length - 1) } else { $relative }
        $components = @($withoutMarker -split '/')
        if ([string]::IsNullOrWhiteSpace($relative) -or $relative -match '[\x00-\x1F\x7F\\:"]' -or
                @($components | Where-Object { $_ -in @("", ".", "..") }).Count -gt 0) {
            throw "Malformed ignored path evidence from Git: $relative. Use -IndependentBuilds."
        }
        # This allowlist applies only to ignored entries. Tracked/nonignored
        # inputs below remain in the raw closure even under these same names.
        if ($withoutMarker -ceq "book") { continue }
        $declaredTree = $components[0] -ceq "artifacts" -and ($isDirectory -or $components.Count -gt 1)
        $directoryComponents = $components.Count
        if (-not $isDirectory) { $directoryComponents-- }
        for ($index = 0; $index -lt $directoryComponents -and -not $declaredTree; $index++) {
            if (@("build", ".gradle", ".kotlin") -ccontains $components[$index]) { $declaredTree = $true }
        }
        if (-not $declaredTree) { $unexpectedIgnored.Add($relative) }
    }
    if ($unexpectedIgnored.Count -gt 0) {
        throw ("Ignored non-generated files need an explicit input contract before canonical reuse. " +
            "Use -IndependentBuilds. First paths: " + (($unexpectedIgnored | Select-Object -First 8) -join ', '))
    }
    $paths = @(Invoke-VerificationGit -RepositoryRoot $RepositoryRoot -Arguments @(
        "ls-files", "--cached", "--others", "--exclude-standard"
    ))
    $paths = @($paths | Sort-Object -Unique -CaseSensitive)
    $records = [Collections.Generic.List[object]]::new()
    [long]$bytes = 0
    foreach ($relative in $paths) {
        $path = Resolve-VerificationChildPath -Root $RepositoryRoot -RelativePath $relative
        if (Test-Path -LiteralPath $path -PathType Container) {
            throw "A tracked directory/submodule needs an explicit input contract: $relative"
        }
        $exists = Test-Path -LiteralPath $path -PathType Leaf
        $length = 0L
        $hash = $null
        if ($exists) {
            $item = Get-Item -LiteralPath $path -Force
            if ($item.Attributes -band [IO.FileAttributes]::ReparsePoint) {
                throw "Linked input needs an explicit verification input contract: $relative"
            }
            $length = $item.Length
            $hash = Get-RawFileSha256 -Path $path
            $bytes += $length
        }
        $records.Add([ordered]@{ path = $relative; exists = $exists; bytes = $length; sha256 = $hash })
    }
    $json = ConvertTo-Json -InputObject @($records) -Depth 10 -Compress
    return [pscustomobject]@{
        Sha256 = Get-TextSha256 $json
        Files = $records.Count
        Bytes = $bytes
        Records = @($records)
    }
}

function Get-VerificationEnvironmentDigest {
    $values = [Environment]::GetEnvironmentVariables([EnvironmentVariableTarget]::Process)
    $rows = @($values.Keys | Sort-Object -CaseSensitive | ForEach-Object {
        [ordered]@{ name = [string]$_; value = [string]$values[$_] }
    })
    # Persist only the digest, never environment values or secret-bearing output.
    return Get-TextSha256 (ConvertTo-Json -InputObject $rows -Depth 5 -Compress)
}

function Get-EffectiveGradleUserHome {
    foreach ($name in @("JAVA_TOOL_OPTIONS", "JDK_JAVA_OPTIONS", "_JAVA_OPTIONS", "JAVA_OPTS", "GRADLE_OPTS")) {
        $value = [Environment]::GetEnvironmentVariable($name, [EnvironmentVariableTarget]::Process)
        if ($value -match '-D(?:user\.home|gradle\.user\.home)=') {
            throw "$name overrides a home directory. Use explicit absolute GRADLE_USER_HOME or independent verification."
        }
        if ($value -match '(?:^|[\s"''])@|-XX:Flags=|-javaagent:|-agentpath:|-agentlib:') {
            throw "$name declares an external JVM input outside the canonical input contract. Use independent verification."
        }
    }
    if (-not [string]::IsNullOrWhiteSpace($env:GRADLE_USER_HOME)) {
        if (-not [IO.Path]::IsPathRooted($env:GRADLE_USER_HOME)) {
            throw "Canonical verification requires an absolute GRADLE_USER_HOME."
        }
        return [IO.Path]::GetFullPath($env:GRADLE_USER_HOME)
    }
    return Join-Path ([Environment]::GetFolderPath([Environment+SpecialFolder]::UserProfile)) ".gradle"
}

function Read-CanonicalGradleProperties {
    param([Parameter(Mandatory)] [string]$Path)
    # Deliberately conservative Java-properties subset. Do not partially decode
    # escaped keys or continuations and then claim the JVM-input guard is complete.
    $properties = [Collections.Generic.Dictionary[string, string]]::new([StringComparer]::Ordinal)
    if (Test-Path -LiteralPath $Path -PathType Leaf) {
        $whitespace = [char[]]@(32, 9, 12)
        $lineNumber = 0
        foreach ($line in ([IO.File]::ReadAllText($Path) -split '\r\n|\r|\n')) {
            $lineNumber++
            $logical = $line.TrimStart($whitespace)
            if ($logical.Length -eq 0 -or $logical.StartsWith("#") -or $logical.StartsWith("!")) { continue }
            if (([regex]::Match($logical, '\\+$').Length % 2) -ne 0) {
                throw "Java-properties line continuation is outside the canonical input contract at line $lineNumber in $Path. Use -IndependentBuilds."
            }
            $keyMatch = [regex]::Match($logical, '^([^=:\t\f ]+)(.*)$')
            if (-not $keyMatch.Success -or $keyMatch.Groups[1].Value.Contains('\')) {
                throw "Empty or escaped Java-properties key is outside the canonical input contract at line $lineNumber in $Path. Use -IndependentBuilds."
            }
            $key = $keyMatch.Groups[1].Value
            $value = $keyMatch.Groups[2].Value.TrimStart($whitespace)
            if ($value.StartsWith("=") -or $value.StartsWith(":")) { $value = $value.Substring(1) }
            $value = $value.TrimStart($whitespace)
            # Java properties are case-sensitive and later duplicate keys win.
            # Preserve trailing value whitespace; do not normalize a custom path.
            $properties[$key] = $value
        }
    }
    return [pscustomobject]@{ Values = $properties }
}

function Assert-CanonicalPropertyJvmArguments {
    param([Parameter(Mandatory)] [string]$Path)
    $properties = Read-CanonicalGradleProperties -Path $Path
    if ($properties.Values.ContainsKey("org.gradle.jvmargs")) {
        $value = $properties.Values["org.gradle.jvmargs"]
        if ($value.Contains('\') -or
                $value -match '-D(?:user\.home|gradle\.user\.home)=|(?:^|[\s"''])@|-XX:Flags=|-javaagent:|-agentpath:|-agentlib:') {
            throw "Escaped or externally dependent org.gradle.jvmargs is outside the canonical input contract: $Path. Use -IndependentBuilds."
        }
    }
}

function Assert-CanonicalBuildRootProperties {
    param([Parameter(Mandatory)] [string]$RepositoryRoot)
    # This is a reviewed three-root contract, not a Gradle settings interpreter.
    # Canonical task batches explicitly set daemon, parallelism and build-cache
    # policy. No other build-local property may acquire a different meaning when
    # a standalone shared/desktop task is consumed through the root composite.
    # Contextual settings/build-logic changes require review or IndependentBuilds.
    $knownKeys = @("org.gradle.daemon", "org.gradle.parallel", "org.gradle.caching", "org.gradle.jvmargs")
    $rootJvmArguments = $null
    foreach ($relative in @("gradle.properties", "source/shared/gradle.properties", "source/desktop/gradle.properties")) {
        $path = Resolve-VerificationChildPath $RepositoryRoot $relative
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
            throw "Canonical three-root property contract requires $relative. Use -IndependentBuilds."
        }
        $properties = Read-CanonicalGradleProperties -Path $path
        foreach ($key in $properties.Values.Keys) {
            if ($key -cnotin $knownKeys) {
                throw "Unsupported build-local Gradle property '$key' in $relative. Canonical context equivalence is not established; use -IndependentBuilds."
            }
            if ($key -cne "org.gradle.jvmargs" -and $properties.Values[$key] -cnotin @("true", "false")) {
                throw "Noncanonical build-policy property '$key' in $relative. Use -IndependentBuilds."
            }
        }
        Assert-CanonicalPropertyJvmArguments -Path $path
        if (-not $properties.Values.ContainsKey("org.gradle.jvmargs")) {
            throw "Canonical three-root property contract requires explicit org.gradle.jvmargs in $relative. Use -IndependentBuilds."
        }
        if ($null -eq $rootJvmArguments) {
            $rootJvmArguments = $properties.Values["org.gradle.jvmargs"]
        } elseif ($properties.Values["org.gradle.jvmargs"] -cne $rootJvmArguments) {
            throw "Different build-root org.gradle.jvmargs in $relative. Canonical context equivalence is not established; use -IndependentBuilds."
        }
    }
}

function Get-ExternalGradleConfiguration {
    param([Parameter(Mandatory)] [string]$RepositoryRoot, [string[]]$ToolchainFiles = @())
    $gradleHome = Get-EffectiveGradleUserHome
    Assert-CanonicalBuildRootProperties -RepositoryRoot $RepositoryRoot
    $paths = [Collections.Generic.List[string]]::new()
    foreach ($relative in @("gradle.properties", "source/shared/gradle.properties", "source/desktop/gradle.properties")) {
        $paths.Add((Join-Path $RepositoryRoot $relative))
    }
    foreach ($name in @("gradle.properties", "init.gradle", "init.gradle.kts")) {
        $paths.Add((Join-Path $gradleHome $name))
    }
    $initDirectories = [Collections.Generic.List[string]]::new()
    $initDirectories.Add((Join-Path $gradleHome "init.d"))
    $wrapperPath = Join-Path $RepositoryRoot "gradle/wrapper/gradle-wrapper.properties"
    $wrapperProperties = Read-CanonicalGradleProperties -Path $wrapperPath
    foreach ($layout in @(
            [pscustomobject]@{ Key = "distributionBase"; Default = "GRADLE_USER_HOME" },
            [pscustomobject]@{ Key = "distributionPath"; Default = "wrapper/dists" })) {
        $effectiveValue = if ($wrapperProperties.Values.ContainsKey($layout.Key)) {
            $wrapperProperties.Values[$layout.Key]
        } else { $layout.Default }
        if ($effectiveValue -cne $layout.Default) {
            throw "Unsupported wrapper $($layout.Key) layout in $wrapperPath. Canonical init-script discovery requires $($layout.Default); use -IndependentBuilds."
        }
    }
    if (-not $wrapperProperties.Values.ContainsKey("distributionUrl")) {
        throw "The pinned wrapper has no effective distributionUrl."
    }
    $distribution = [regex]::Match($wrapperProperties.Values["distributionUrl"],
        '/(gradle-([0-9A-Za-z.-]+)-(?:bin|all))\.zip$')
    if (-not $distribution.Success) {
        throw "Cannot determine the pinned wrapper distribution for input provenance."
    }
    $distributionRoot = Join-Path $gradleHome ("wrapper/dists/" + $distribution.Groups[1].Value)
    if (Test-Path -LiteralPath $distributionRoot -PathType Container) {
        foreach ($directory in @(Get-ChildItem -LiteralPath $distributionRoot -Directory -Force)) {
            $distributionHome = Join-Path $directory.FullName ("gradle-" + $distribution.Groups[2].Value)
            $initDirectories.Add((Join-Path $distributionHome "init.d"))
            $paths.Add((Join-Path $distributionHome "gradle.properties"))
        }
    }
    foreach ($directory in $initDirectories) {
        # Directory membership is part of the digest, including absent directories.
        $paths.Add($directory)
        if (Test-Path -LiteralPath $directory -PathType Container) {
            foreach ($file in @(Get-ChildItem -LiteralPath $directory -File -Force)) {
                $paths.Add($file.FullName)
            }
        }
    }
    foreach ($path in $ToolchainFiles) { $paths.Add($path) }
    $records = foreach ($path in @($paths | Sort-Object -Unique -CaseSensitive)) {
        $file = Test-Path -LiteralPath $path -PathType Leaf
        $directory = Test-Path -LiteralPath $path -PathType Container
        if ($file -and [IO.Path]::GetFileName($path) -ceq "gradle.properties") {
            Assert-CanonicalPropertyJvmArguments -Path $path
        }
        if ($file -and $path -match '(?:^|[\\/])(?:init\.gradle(?:\.kts)?|[^\\/]+\.gradle(?:\.kts)?)$') {
            throw "Custom Gradle init script is outside the canonical input contract: $path. Use -IndependentBuilds."
        }
        [ordered]@{
            path = $path
            kind = if ($file) { "file" } elseif ($directory) { "directory" } else { "absent" }
            sha256 = if ($file) { Get-RawFileSha256 $path } else { $null }
        }
    }
    return [pscustomobject]@{
        GradleUserHome = $gradleHome
        Sha256 = Get-TextSha256 (ConvertTo-Json -InputObject @($records) -Depth 5 -Compress)
        Files = @($records)
    }
}

function Get-VerificationInputIdentity {
    param([Parameter(Mandatory)] [string]$RepositoryRoot, [string[]]$ToolchainFiles = @())
    $watch = [Diagnostics.Stopwatch]::StartNew()
    $head = (@(Invoke-VerificationGit $RepositoryRoot @("rev-parse", "HEAD")) -join "").Trim()
    $index = @(Invoke-VerificationGit $RepositoryRoot @("ls-files", "--stage"))
    $status = @(Invoke-VerificationGit $RepositoryRoot @("status", "--porcelain=v1", "--untracked-files=all"))
    $raw = Get-RawInputInventory $RepositoryRoot
    $external = Get-ExternalGradleConfiguration -RepositoryRoot $RepositoryRoot -ToolchainFiles $ToolchainFiles
    $identity = [ordered]@{
        head = $head
        indexSha256 = Get-TextSha256 ($index -join $script:Lf)
        statusSha256 = Get-TextSha256 ($status -join $script:Lf)
        rawTreeSha256 = $raw.Sha256
        rawFiles = $raw.Files
        rawBytes = $raw.Bytes
        environmentSha256 = Get-VerificationEnvironmentDigest
        externalConfigurationSha256 = $external.Sha256
        gradleUserHome = $external.GradleUserHome
    }
    $watch.Stop()
    return [pscustomobject]@{
        Fingerprint = Get-TextSha256 ($identity | ConvertTo-Json -Depth 10 -Compress)
        Identity = $identity
        RawInventory = $raw.Records
        ExternalConfiguration = $external.Files
        ElapsedSeconds = $watch.Elapsed.TotalSeconds
    }
}

function Assert-InputIdentity {
    param([Parameter(Mandatory)] [object]$Expected, [Parameter(Mandatory)] [object]$Actual)
    if ($Expected.Fingerprint -cne $Actual.Fingerprint) {
        throw "Verification inputs/environment changed during this invocation; canonical evidence is invalid."
    }
}

function Read-VerificationXml {
    param([Parameter(Mandatory)] [string]$Path)
    $settings = [Xml.XmlReaderSettings]::new()
    $settings.DtdProcessing = [Xml.DtdProcessing]::Prohibit
    $settings.XmlResolver = $null
    $reader = [Xml.XmlReader]::Create($Path, $settings)
    try {
        $document = [Xml.XmlDocument]::new()
        $document.XmlResolver = $null
        $document.Load($reader)
        return $document
    } finally {
        $reader.Dispose()
    }
}

function Get-JUnitEvidence {
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [ValidateSet("shared", "desktop")] [string]$Module,
        [Parameter(Mandatory)] [string]$ArchiveDirectory,
        [Parameter(Mandatory)] [ValidateSet("DEV", "COMPOSED", "FULL")] [string]$Level
    )
    $directory = Resolve-VerificationChildPath $RepositoryRoot $script:ModuleDefinitions[$Module].ResultDirectory
    $files = @(Get-ChildItem -LiteralPath $directory -Filter "TEST-*.xml" -File -ErrorAction Stop)
    if ($files.Count -eq 0) { throw "No fresh JUnit reports for $Module." }
    [void][IO.Directory]::CreateDirectory($ArchiveDirectory)
    $reports = [Collections.Generic.List[object]]::new()
    foreach ($file in $files) {
        $document = Read-VerificationXml $file.FullName
        $suite = $document.DocumentElement
        # XML attributes (notably testsuite/@name) shadow adapted properties in
        # PowerShell. Structural checks must use the underlying XmlElement.
        if ($null -eq $suite -or $suite.psbase.Name -cne "testsuite" -or
                $suite.psbase.NamespaceURI -cne "") {
            throw "Unsupported JUnit report root: $($file.FullName)"
        }
        if (@($suite.SelectNodes(".//*") | Where-Object {
                $_.psbase.NamespaceURI -cne ""
            }).Count -gt 0 -or @($suite.SelectNodes("*") | Where-Object {
                $_.psbase.Name -cnotin @("properties", "testcase", "system-out", "system-err")
            }).Count -gt 0) {
            throw "Unsupported JUnit report element: $($file.FullName)"
        }
        $className = $suite.GetAttribute("name")
        $cases = @($suite.SelectNodes("testcase"))
        foreach ($case in $cases) {
            if (@($case.SelectNodes("*") | Where-Object {
                    $_.psbase.Name -cnotin @("failure", "error", "skipped", "properties", "system-out", "system-err")
                }).Count -gt 0) {
                throw "Unsupported JUnit testcase element: $($file.FullName)"
            }
        }
        $tests = [int]$suite.GetAttribute("tests")
        $failures = [int]$suite.GetAttribute("failures")
        $errors = [int]$suite.GetAttribute("errors")
        $skipped = [int]$suite.GetAttribute("skipped")
        if ($tests -ne $cases.Count) { throw "JUnit case/count mismatch: $className" }
        $caseRecords = @($cases | ForEach-Object {
            $state = if ($_.SelectNodes("failure").Count -gt 0) { "FAILURE" } elseif ($_.SelectNodes("error").Count -gt 0) { "ERROR" } elseif ($_.SelectNodes("skipped").Count -gt 0) { "SKIPPED" } else { "PASS" }
            [ordered]@{ class = $_.GetAttribute("classname"); name = $_.GetAttribute("name"); status = $state }
        } | Sort-Object { $_.class }, { $_.name })
        if (@($caseRecords | Where-Object status -eq "FAILURE").Count -ne $failures -or
            @($caseRecords | Where-Object status -eq "ERROR").Count -ne $errors -or
            @($caseRecords | Where-Object status -eq "SKIPPED").Count -ne $skipped) {
            throw "JUnit result counters do not match case outcomes: $className"
        }
        $archive = Join-Path $ArchiveDirectory $file.Name
        Copy-Item -LiteralPath $file.FullName -Destination $archive -Force
        $hash = Get-RawFileSha256 $file.FullName
        if ((Get-RawFileSha256 $archive) -cne $hash) { throw "JUnit evidence copy changed: $className" }
        $reports.Add([ordered]@{
            module = $Module
            class = $className
            livePath = [IO.Path]::GetRelativePath($RepositoryRoot, $file.FullName).Replace("\", "/")
            archivePath = $archive
            sha256 = $hash
            tests = $tests
            failures = $failures
            errors = $errors
            skipped = $skipped
            cases = $caseRecords
        })
    }
    $failed = @($reports | Where-Object { $_.failures -gt 0 -or $_.errors -gt 0 })
    if ($failed.Count -gt 0) {
        throw "JUnit failures/errors in $Module despite any Gradle ignoreFailures setting: $($failed.class -join ', '). Archived XML: $ArchiveDirectory"
    }
    $mandatorySkipped = @($reports | Where-Object {
        $_.skipped -gt 0 -and ($Level -ne "FULL" -or $_.class.StartsWith("org.geocedg.", [StringComparison]::Ordinal) -or $_.class -cin $script:MandatoryUpstreamClasses)
    })
    if ($mandatorySkipped.Count -gt 0) { throw "Mandatory tests were skipped: $($mandatorySkipped.class -join ', ')" }
    if ((@($reports.tests) | Measure-Object -Sum).Sum -le 0) { throw "Zero-test execution is not verification evidence." }
    return @($reports)
}

function Get-GradleTaskEvidence {
    param([Parameter(Mandatory)] [string]$Text)
    $headings = @([regex]::Matches($Text, '(?m)^> Task (:\S+?)(?: (UP-TO-DATE|FROM-CACHE|NO-SOURCE|SKIPPED|FAILED))?[\t ]*\r?$') | ForEach-Object {
        [pscustomobject][ordered]@{
            task = $_.Groups[1].Value
            outcome = if ($_.Groups[2].Success) { $_.Groups[2].Value } else { "EXECUTED" }
        }
    })
    # --info can repeat the same task heading when switching output groups.
    # A heading is not a second task execution; conflicting outcomes are unsafe.
    return @($headings | Group-Object -Property task | ForEach-Object {
        $outcomes = @($_.Group.outcome | Sort-Object -Unique)
        [ordered]@{
            task = $_.Name
            outcome = if ($outcomes.Count -eq 1) { $outcomes[0] } else { "CONFLICTING_LOG_OUTCOMES" }
            headingOccurrences = $_.Count
            observedOutcomes = $outcomes
        }
    })
}

function Assert-FreshTestTask {
    param([Parameter(Mandatory)] [object[]]$Tasks, [Parameter(Mandatory)] [string]$Task)
    $matches = @($Tasks | Where-Object { $_.task -ceq $Task })
    if ($matches.Count -ne 1 -or $matches[0].outcome -cne "EXECUTED") {
        throw "Mandatory Test task did not provide unambiguous fresh execution evidence: $Task"
    }
}

function Get-SelectedTestJvmEvidence {
    param([Parameter(Mandatory)] [string]$Text, [Parameter(Mandatory)] [string[]]$ToolchainFiles)
    $executors = @([regex]::Matches($Text,
        '(?m)^Starting process ''Gradle Test Executor \d+''\.[^\r\n]*?Command: (?:"([^"\r\n]+[\\/]java\.exe)"|([^\r\n]+?[\\/]java\.exe))(?:\s|$)'))
    if ($executors.Count -eq 0) {
        throw "A fresh Test task has no inspectable --info test-JVM launch evidence."
    }
    $paths = @($executors | ForEach-Object {
        $value = if ($_.Groups[1].Success) { $_.Groups[1].Value } else { $_.Groups[2].Value }
        [IO.Path]::GetFullPath($value)
    } | Sort-Object -Unique)
    foreach ($path in $paths) {
        if (@($ToolchainFiles | Where-Object {
                ([IO.Path]::GetFullPath($_)).Equals($path, [StringComparison]::OrdinalIgnoreCase)
            }).Count -eq 0) {
            throw "Selected Test JVM was not bound by the toolchain inventory: $path"
        }
        [ordered]@{ path = $path; sha256 = Get-RawFileSha256 $path }
    }
}

function Invoke-VerificationNative {
    param(
        [Parameter(Mandatory)] [string]$FilePath,
        [Parameter(Mandatory)] [string[]]$Arguments,
        [Parameter(Mandatory)] [string]$WorkingDirectory,
        [Parameter(Mandatory)] [string]$LogPath,
        [Parameter(Mandatory)] [string]$Description
    )
    # Native exit status is captured explicitly, including on hosts opting into
    # PowerShell's native-error promotion. This preference is function-local.
    $PSNativeCommandUseErrorActionPreference = $false
    [void][IO.Directory]::CreateDirectory((Split-Path -Parent $LogPath))
    $started = [datetime]::UtcNow
    $watch = [Diagnostics.Stopwatch]::StartNew()
    Write-Host ("==> " + $Description)
    Write-Host ($FilePath + " " + ($Arguments -join " "))
    $lines = [Collections.Generic.List[string]]::new()
    $nativeExitCode = $null
    $launchFailure = $null
    $logFailure = $null
    Push-Location $WorkingDirectory
    try {
        $global:LASTEXITCODE = $null
        & $FilePath @Arguments 2>&1 | ForEach-Object {
            $line = $_.ToString()
            $lines.Add($line)
            Write-Host $line
        }
        $nativeExitCode = $global:LASTEXITCODE
    } catch {
        $launchFailure = $_.Exception.Message
    } finally {
        Pop-Location
        $watch.Stop()
        try {
            [IO.File]::WriteAllText($LogPath, ($lines -join $script:Lf) + $script:Lf, [Text.UTF8Encoding]::new($false))
        } catch { $logFailure = $_.Exception.Message }
    }
    if ($launchFailure -or $logFailure -or $null -eq $nativeExitCode) {
        throw ("Native command/provenance failed. Exit=$nativeExitCode; " +
            "launch=$launchFailure; log=$logFailure; log path=$LogPath")
    }
    return [pscustomobject]@{
        file = $FilePath
        arguments = $Arguments
        workingDirectory = $WorkingDirectory
        logPath = $LogPath
        startedUtc = $started.ToString("o")
        finishedUtc = [datetime]::UtcNow.ToString("o")
        elapsedSeconds = $watch.Elapsed.TotalSeconds
        exitCode = $nativeExitCode
        text = $lines -join $script:Lf
    }
}

function ConvertTo-GeoCeDGIncrementalGradleArguments {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [string[]]$Arguments, [switch]$KeepBuildOutputs)
    Assert-LoadedRuntimeSource
    $result = [Collections.Generic.List[string]]::new()
    $removed = @("--rerun-tasks", "--no-build-cache", "--build-cache", "--daemon", "--no-daemon", "--configuration-cache", "--no-configuration-cache", "--parallel", "--no-parallel")
    for ($index = 0; $index -lt $Arguments.Count; $index++) {
        $argument = $Arguments[$index]
        if ($argument -cin $removed) { continue }
        $result.Add($argument)
        if ($argument -cmatch '^:.*:test$' -and ($index + 1 -ge $Arguments.Count -or $Arguments[$index + 1] -cne "--rerun")) {
            $result.Add("--rerun")
        }
    }
    if (@($result | Where-Object { $_ -match '^--max-workers(?:=|$)' }).Count -eq 0) {
        $result.Add("--max-workers=1")
    }
    $result.Add("--build-cache")
    $result.Add("--no-configuration-cache")
    $result.Add("--no-parallel")
    $result.Add($(if ($KeepBuildOutputs) { "--daemon" } else { "--no-daemon" }))
    return @($result)
}

function Assert-GeoCeDGChildVerificationMode {
    [CmdletBinding()]
    param([switch]$SkipBuild, [AllowEmptyString()] [string]$BuildEvidencePath, [switch]$IncrementalBuild)
    Assert-LoadedRuntimeSource
    if (-not [string]::IsNullOrWhiteSpace($BuildEvidencePath) -and ($SkipBuild -or $IncrementalBuild)) {
        throw "Current-run evidence consumption cannot be combined with SkipBuild or IncrementalBuild."
    }
    if ($SkipBuild -and $IncrementalBuild) {
        throw "IncrementalBuild requires actual build/test execution; SkipBuild is static-only."
    }
}

function Remove-CurrentJUnitReports {
    param([Parameter(Mandatory)] [string]$RepositoryRoot, [Parameter(Mandatory)] [string]$Module)
    $relative = $script:ModuleDefinitions[$Module].ResultDirectory
    $directory = Resolve-VerificationChildPath $RepositoryRoot $relative
    if ($relative -notmatch '/build/test-results/test$') { throw "Unexpected JUnit output target." }
    # KeepBuildOutputs skips the expensive snapshot, not physical-path safety.
    # Validate every existing ancestor, including nested report-directory links,
    # before enumeration or removal can reach outside this repository.
    $root = [IO.Path]::GetFullPath($RepositoryRoot).TrimEnd('/', '\')
    $ancestor = $directory
    while ($true) {
        if ((Test-Path -LiteralPath $ancestor) -and
                ((Get-Item -LiteralPath $ancestor -Force).Attributes -band [IO.FileAttributes]::ReparsePoint)) {
            throw "Refusing to remove JUnit reports through a linked path: $ancestor"
        }
        if ($ancestor.Equals($root, [StringComparison]::OrdinalIgnoreCase)) { break }
        $ancestor = Split-Path -Parent $ancestor
        if ([string]::IsNullOrWhiteSpace($ancestor)) { throw "JUnit output ancestry does not reach the repository." }
    }
    if (Test-Path -LiteralPath $directory -PathType Container) {
        $files = @(Get-ChildItem -LiteralPath $directory -Filter "TEST-*.xml" -File)
        foreach ($file in $files) {
            if ($file.Attributes -band [IO.FileAttributes]::ReparsePoint) {
                throw "Refusing to remove a linked JUnit report: $($file.FullName)"
            }
        }
        foreach ($file in $files) {
            # Exact generated XML files only. The caller owns the output transaction.
            Remove-Item -LiteralPath $file.FullName -Force
        }
    }
}

function Clear-GeoCeDGIndependentFullTestReports {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [ValidateSet("shared", "desktop")] [string]$Module
    )
    Assert-LoadedRuntimeSource
    if ($null -ne $script:ActiveEvidence) {
        throw "Independent FULL cannot clear JUnit reports while canonical evidence is active."
    }
    # The existing baseline snapshot (or explicit KeepBuildOutputs) owns cleanup.
    Remove-CurrentJUnitReports -RepositoryRoot $RepositoryRoot -Module $Module
}

function Assert-GeoCeDGIndependentFullTestOutcome {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [ValidateSet("shared", "desktop")] [string]$Module,
        [Parameter(Mandatory)] [string]$WorkingDirectory,
        [Parameter(Mandatory)] [string]$LogPath,
        [Parameter(Mandatory)] [string]$ArchiveDirectory
    )
    Assert-LoadedRuntimeSource
    # Gradle may exit zero with ignoreFailures (including the CI profile).
    # Read every fresh suite before the baseline restores generated outputs.
    $reports = @(Get-JUnitEvidence -RepositoryRoot $RepositoryRoot -Module $Module `
        -ArchiveDirectory $ArchiveDirectory -Level FULL)
    $tasks = @(Get-GradleTaskEvidence -Text ([IO.File]::ReadAllText($LogPath)))
    foreach ($task in $tasks) {
        $task.task = ConvertTo-CanonicalTaskName -Task $task.task `
            -WorkingDirectory $WorkingDirectory -RepositoryRoot $RepositoryRoot
    }
    Assert-FreshTestTask -Tasks $tasks -Task $script:ModuleDefinitions[$Module].Task
    Write-Host ("Independent FULL {0}: inspected {1} fresh tests. XML: {2}" -f `
        $Module, (@($reports.tests) | Measure-Object -Sum).Sum, $ArchiveDirectory)
}

function Get-TestBuildArguments {
    param(
        [Parameter(Mandatory)] [string]$Module,
        [string[]]$Filters = @(),
        [switch]$IncludeCheckstyle,
        [switch]$AllowToolchainDownload,
        [switch]$KeepBuildOutputs,
        [switch]$RebuildDependencies
    )
    $arguments = @($script:ModuleDefinitions[$Module].Task)
    foreach ($filter in $Filters) { $arguments += @("--tests", $filter) }
    if ($IncludeCheckstyle) { $arguments += @($script:ModuleDefinitions[$Module].Styles.Keys) }
    $arguments += @("--info", "--profile", "--console=plain", "--no-problems-report")
    if (-not $AllowToolchainDownload) { $arguments += "-Dorg.gradle.java.installations.auto-download=false" }
    $arguments = @(ConvertTo-GeoCeDGIncrementalGradleArguments -Arguments $arguments -KeepBuildOutputs:$KeepBuildOutputs)
    if ($RebuildDependencies) {
        $arguments = @($arguments | Where-Object { $_ -cne "--build-cache" }) + @("--no-build-cache", "--rerun-tasks")
    }
    return $arguments
}

function Get-CheckstyleEvidence {
    param([Parameter(Mandatory)] [string]$RepositoryRoot, [Parameter(Mandatory)] [string]$ArchiveDirectory)
    [void][IO.Directory]::CreateDirectory($ArchiveDirectory)
    $results = foreach ($module in $script:ModuleDefinitions.Keys) {
        foreach ($entry in $script:ModuleDefinitions[$module].Styles.GetEnumerator()) {
            $path = Resolve-VerificationChildPath $RepositoryRoot $entry.Value
            $document = Read-VerificationXml $path
            $root = $document.DocumentElement
            if ($null -eq $root -or $root.psbase.Name -cne "checkstyle" -or
                    $root.psbase.NamespaceURI -cne "" -or
                    @($root.SelectNodes(".//*") | Where-Object {
                        $_.psbase.NamespaceURI -cne "" -or
                            $_.psbase.Name -cnotin @("file", "error")
                    }).Count -gt 0 -or
                    @($root.SelectNodes("*") | Where-Object { $_.psbase.Name -cne "file" }).Count -gt 0 -or
                    $document.SelectNodes("//error").Count -ne 0) {
                throw "Checkstyle evidence is not clean: $($entry.Value)"
            }
            $archive = Join-Path $ArchiveDirectory (($entry.Key.TrimStart(":").Replace(":", "-")) + ".xml")
            Copy-Item -LiteralPath $path -Destination $archive -Force
            $hash = Get-RawFileSha256 $path
            if ((Get-RawFileSha256 $archive) -cne $hash) { throw "Checkstyle evidence copy changed." }
            [ordered]@{ task = $entry.Key; livePath = $entry.Value; archivePath = $archive; sha256 = $hash }
        }
    }
    return @($results)
}

function Invoke-GeoCeDGCanonicalBuild {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [ValidateSet("COMPOSED", "FULL")] [string]$Level,
        [Parameter(Mandatory)] [string]$LogDirectory,
        [switch]$AllowToolchainDownload,
        [switch]$KeepBuildOutputs,
        [switch]$RebuildDependencies
    )
    Assert-LoadedRuntimeSource
    if ($null -ne $script:ActiveEvidence) { throw "A canonical verification run is already active in this module." }
    $RepositoryRoot = [IO.Path]::GetFullPath($RepositoryRoot)
    $runId = [guid]::NewGuid().ToString("N")
    $owner = [guid]::NewGuid().ToString("N")
    $runRoot = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) $runId
    [void][IO.Directory]::CreateDirectory($runRoot)
    $receiptPath = Join-Path $runRoot "build-evidence.json"
    $script:ActiveEvidence = [pscustomobject]@{
        State = "RUNNING"; Owner = $owner; RunId = $runId; ModuleIdentity = $script:ModuleIdentity
        ProcessId = $PID; RunspaceId = [System.Management.Automation.Runspaces.Runspace]::DefaultRunspace.InstanceId
        RepositoryRoot = $RepositoryRoot; ReceiptPath = $receiptPath; ReceiptHash = $null
        Inputs = $null; ToolchainFiles = @(); Receipt = $null
    }
    $nativeRuns = [Collections.Generic.List[object]]::new()
    try {
        [void](Get-ExternalGradleConfiguration -RepositoryRoot $RepositoryRoot)
        $wrapper = Join-Path $RepositoryRoot "gradlew.bat"
        $version = Invoke-VerificationNative -FilePath $wrapper -Arguments @("--version", "--no-daemon", "--no-problems-report") -WorkingDirectory $RepositoryRoot -LogPath (Join-Path $runRoot "gradle-version.log") -Description "Canonical wrapper identity"
        $nativeRuns.Add($version)
        if ($version.exitCode -ne 0) { throw "Gradle version probe failed with exit $($version.exitCode): $($version.logPath)" }
        # The wrapper may have installed the pinned distribution. Check its init
        # directory before any configuration-running probe, not after execution.
        [void](Get-ExternalGradleConfiguration -RepositoryRoot $RepositoryRoot)
        if ($AllowToolchainDownload) {
            # Explicit opt-in may resolve a previously absent compiler/test JDK.
            # Resolve it before sealing the input/toolchain inventory; no Test
            # task runs in this preparation call. Default provisioned runs omit it.
            $prepareArguments = @(ConvertTo-GeoCeDGIncrementalGradleArguments -Arguments @(
                ":shared:common-jre:compileTestJava", ":desktop:desktop:compileTestJava",
                "--info", "--console=plain", "--no-problems-report"
            ) -KeepBuildOutputs:$KeepBuildOutputs)
            $prepare = Invoke-VerificationNative -FilePath $wrapper -Arguments $prepareArguments `
                -WorkingDirectory $RepositoryRoot -LogPath (Join-Path $runRoot "toolchain-preparation.log") `
                -Description "Explicitly allowed compiler/test-toolchain preparation (no tests)"
            $nativeRuns.Add($prepare)
            if ($prepare.exitCode -ne 0) {
                throw "Toolchain preparation failed with exit $($prepare.exitCode): $($prepare.logPath)"
            }
        }
        $toolchainArguments = @("-q", "javaToolchains", "--no-daemon", "--no-configuration-cache", "--no-problems-report", "--console=plain")
        if (-not $AllowToolchainDownload) { $toolchainArguments += "-Dorg.gradle.java.installations.auto-download=false" }
        $toolchains = Invoke-VerificationNative -FilePath $wrapper -Arguments $toolchainArguments -WorkingDirectory $RepositoryRoot -LogPath (Join-Path $runRoot "java-toolchains.log") -Description "Canonical toolchain identity"
        $nativeRuns.Add($toolchains)
        if ($toolchains.exitCode -ne 0) { throw "Gradle toolchain probe failed with exit $($toolchains.exitCode): $($toolchains.logPath)" }
        $toolchainLocations = @([regex]::Matches($toolchains.text, '(?m)^[\t ]*(?:\|[\t ]*)?Location:[\t ]*([^\r\n]+)') | ForEach-Object {
            $_.Groups[1].Value.Trim()
        } | Sort-Object -Unique)
        if ($toolchainLocations.Count -eq 0) {
            throw "No inspectable JVM locations in the Gradle toolchain report; refusing an unbound receipt."
        }
        $toolchainFiles = @($toolchainLocations | ForEach-Object {
            $location = $_
            if (-not [IO.Path]::IsPathRooted($location) -or
                    -not (Test-Path -LiteralPath (Join-Path $location 'release') -PathType Leaf) -or
                    -not (Test-Path -LiteralPath (Join-Path $location 'bin/java.exe') -PathType Leaf)) {
                throw "Gradle reported an uninspectable JVM location: $location"
            }
            foreach ($relative in @("release", "bin/java.exe", "bin/javac.exe")) { Join-Path $location $relative }
        })
        $script:ActiveEvidence.ToolchainFiles = $toolchainFiles
        $before = Get-VerificationInputIdentity -RepositoryRoot $RepositoryRoot -ToolchainFiles $toolchainFiles
        $script:ActiveEvidence.Inputs = $before
        Write-VerificationJson -Path (Join-Path $runRoot "input-inventory.json") -Value $before.RawInventory
        Write-VerificationJson -Path (Join-Path $runRoot "external-configuration.json") -Value $before.ExternalConfiguration
        $reports = [Collections.Generic.List[object]]::new()
        $taskEvidence = [Collections.Generic.List[object]]::new()
        $testJvms = [ordered]@{}
        $selections = [ordered]@{}
        foreach ($module in @("shared", "desktop")) {
            Remove-CurrentJUnitReports -RepositoryRoot $RepositoryRoot -Module $module
            $filters = @()
            if ($Level -eq "COMPOSED") {
                $filters = @("org.geocedg.*")
                if ($module -eq "shared") { $filters += $script:MandatoryUpstreamClasses }
            }
            $selections[$module] = [ordered]@{ task = $script:ModuleDefinitions[$module].Task; unfiltered = ($Level -eq "FULL"); filters = $filters }
            $arguments = Get-TestBuildArguments -Module $module -Filters $filters -IncludeCheckstyle -AllowToolchainDownload:$AllowToolchainDownload -KeepBuildOutputs:$KeepBuildOutputs -RebuildDependencies:$RebuildDependencies
            $run = Invoke-VerificationNative -FilePath $wrapper -Arguments $arguments -WorkingDirectory $RepositoryRoot -LogPath (Join-Path $runRoot "$module-gradle.log") -Description "Canonical $Level $module tests and Checkstyle"
            $nativeRuns.Add($run)
            $tasks = @(Get-GradleTaskEvidence $run.text)
            foreach ($task in $tasks) { $taskEvidence.Add($task) }
            $reportFailure = $null
            try {
                $moduleReports = @(Get-JUnitEvidence -RepositoryRoot $RepositoryRoot -Module $module -ArchiveDirectory (Join-Path $runRoot "junit/$module") -Level $Level)
            } catch { $reportFailure = $_.Exception.Message }
            if ($run.exitCode -ne 0) {
                throw "Canonical $module Gradle execution failed with exit $($run.exitCode): $($run.logPath). XML inspection: $reportFailure"
            }
            if ($reportFailure) { throw $reportFailure }
            foreach ($report in $moduleReports) { $reports.Add($report) }
            Assert-FreshTestTask -Tasks $tasks -Task $script:ModuleDefinitions[$module].Task
            $testJvms[$module] = @(Get-SelectedTestJvmEvidence -Text $run.text -ToolchainFiles $toolchainFiles)
            Assert-InputIdentity -Expected $before -Actual (Get-VerificationInputIdentity -RepositoryRoot $RepositoryRoot -ToolchainFiles $toolchainFiles)
        }
        $styles = @(Get-CheckstyleEvidence -RepositoryRoot $RepositoryRoot -ArchiveDirectory (Join-Path $runRoot "checkstyle"))
        foreach ($style in $styles) {
            if (@($taskEvidence | Where-Object { $_.task -ceq $style.task -and
                    $_.outcome -cin @("EXECUTED", "UP-TO-DATE", "FROM-CACHE") }).Count -eq 0) {
                throw "Required Checkstyle task was not validated by Gradle: $($style.task)"
            }
        }
        $auditArtifacts = @(
            (Join-Path $runRoot "input-inventory.json"),
            (Join-Path $runRoot "external-configuration.json")
        ) + @($nativeRuns.logPath)
        $auditInventory = @($auditArtifacts | ForEach-Object {
            [ordered]@{ path = $_; sha256 = Get-RawFileSha256 $_ }
        })
        $receipt = [ordered]@{
            schemaVersion = 1; kind = "CURRENT_RUN_BUILD_EVIDENCE"; runId = $runId
            level = $Level; repositoryRoot = $RepositoryRoot
            state = "TEST_EXECUTION_VERIFIED_PHASE_ASSERTIONS_PENDING"
            authorApproved = $false; selfApproved = $false
            inputFingerprint = $before.Fingerprint; inputIdentity = $before.Identity
            initialFingerprintSeconds = $before.ElapsedSeconds
            allowToolchainDownload = [bool]$AllowToolchainDownload
            testResultReuseAcrossRuns = $false; configurationCache = $false
            newTestParallelism = $false; selections = $selections
            selectedTestJvms = $testJvms; auditArtifacts = $auditInventory
            tasks = @($taskEvidence); junit = @($reports); checkstyle = $styles
            nativeRuns = @($nativeRuns | Select-Object file, arguments, workingDirectory, logPath, startedUtc, finishedUtc, elapsedSeconds, exitCode)
            tests = (@($reports.tests) | Measure-Object -Sum).Sum
            skippedUpstreamTests = (@($reports.skipped) | Measure-Object -Sum).Sum
            sealedUtc = [datetime]::UtcNow.ToString("o")
        }
        Assert-InputIdentity -Expected $before -Actual (Get-VerificationInputIdentity -RepositoryRoot $RepositoryRoot -ToolchainFiles $toolchainFiles)
        Write-VerificationJson $receiptPath $receipt
        $script:ActiveEvidence.Receipt = $receipt
        $script:ActiveEvidence.ReceiptHash = Get-RawFileSha256 $receiptPath
        $script:ActiveEvidence.State = "SEALED"
        Write-Host "Canonical test execution complete: $($receipt.tests) tests. Individual phase assertions still required."
        Write-Host "Build evidence: $receiptPath"
        return [pscustomobject]@{ EvidencePath = $receiptPath; OwnerToken = $owner; RunId = $runId; Tests = $receipt.tests }
    } catch {
        $failure = $_
        try {
            Write-VerificationJson -Path (Join-Path $runRoot "failed-build.json") -Value ([ordered]@{
                runId = $runId; state = "FAILED_NO_CONSUMABLE_RECEIPT"; message = $failure.Exception.Message
                nativeRuns = @($nativeRuns | Select-Object logPath, startedUtc, finishedUtc, elapsedSeconds, exitCode)
            })
        } catch {
            $failure.Exception.Data["DiagnosticPublicationFailure"] = $_.Exception.Message
            Write-Warning "Unable to save failed-build diagnostics: $($_.Exception.Message)" -WarningAction Continue
        } finally {
            if ($null -ne $script:ActiveEvidence -and $script:ActiveEvidence.Owner -ceq $owner) { $script:ActiveEvidence = $null }
        }
        throw $failure
    }
}

function ConvertTo-CanonicalTaskName {
    param([Parameter(Mandatory)] [string]$Task, [Parameter(Mandatory)] [string]$WorkingDirectory, [Parameter(Mandatory)] [string]$RepositoryRoot)
    # Lexical normalization also labels an independent run's own fresh output;
    # it does not establish cross-context execution equivalence. Only receipt
    # consumers require Assert-CanonicalBuildRootProperties before using aliases.
    $cwd = [IO.Path]::GetFullPath($WorkingDirectory).TrimEnd("/", "\")
    $root = [IO.Path]::GetFullPath($RepositoryRoot).TrimEnd("/", "\")
    if ($cwd.Equals($root, [StringComparison]::OrdinalIgnoreCase)) { return $Task }
    if ($cwd.Equals((Join-Path $root "source/shared"), [StringComparison]::OrdinalIgnoreCase)) { return ":shared" + $Task }
    if ($cwd.Equals((Join-Path $root "source/desktop"), [StringComparison]::OrdinalIgnoreCase)) { return ":desktop" + $Task }
    throw "Unsupported Gradle working-directory context: $WorkingDirectory"
}

function Get-RequestedBuildRequirements {
    param([Parameter(Mandatory)] [string[]]$Arguments, [Parameter(Mandatory)] [string]$WorkingDirectory, [Parameter(Mandatory)] [string]$RepositoryRoot)
    Assert-CanonicalBuildRootProperties -RepositoryRoot $RepositoryRoot
    $requirements = [Collections.Generic.List[object]]::new()
    $current = $null
    for ($index = 0; $index -lt $Arguments.Count; $index++) {
        $argument = $Arguments[$index]
        if ($argument.StartsWith(":", [StringComparison]::Ordinal)) {
            $current = [pscustomobject]@{ Task = ConvertTo-CanonicalTaskName $argument $WorkingDirectory $RepositoryRoot; Filters = [Collections.Generic.List[string]]::new() }
            $requirements.Add($current)
        } elseif ($argument -ceq "--tests") {
            if ($null -eq $current -or $current.Task -notmatch ':test$' -or $index + 1 -ge $Arguments.Count) { throw "Test filter has no explicit test-task context." }
            $index++
            $current.Filters.Add($Arguments[$index])
        } elseif ($argument -match '^--(?:rerun(?:-tasks)?|(?:no-)?build-cache|(?:no-)?daemon|no-configuration-cache|no-parallel|no-problems-report|info|profile|stacktrace|console=plain|max-workers=1)$') {
            continue
        } elseif ($argument -ceq "-Dorg.gradle.java.installations.auto-download=false") {
            continue
        } else {
            throw "Unsupported build-evidence argument/context: $argument"
        }
    }
    if ($requirements.Count -eq 0) { throw "No explicit Gradle tasks requested for evidence consumption." }
    return @($requirements)
}

function Assert-ReportHash {
    param([Parameter(Mandatory)] [object]$Report, [Parameter(Mandatory)] [string]$RepositoryRoot)
    $live = Resolve-VerificationChildPath $RepositoryRoot $Report.livePath
    foreach ($path in @($live, [string]$Report.archivePath)) {
        if (-not (Test-Path -LiteralPath $path -PathType Leaf) -or (Get-RawFileSha256 $path) -cne $Report.sha256) {
            throw "Missing or altered current-run report: $path"
        }
    }
}

function Confirm-GeoCeDGBuildEvidence {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$EvidencePath,
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [string]$WorkingDirectory,
        [Parameter(Mandatory)] [string[]]$Arguments,
        [Parameter(Mandatory)] [string]$LogPath,
        [Parameter(Mandatory)] [string]$Description,
        [switch]$AllowToolchainDownload
    )
    Assert-LoadedRuntimeSource
    $active = $script:ActiveEvidence
    if ($null -eq $active -or $active.State -cne "SEALED" -or $active.ModuleIdentity -cne $script:ModuleIdentity -or $active.ProcessId -ne $PID -or $active.RunspaceId -ne [System.Management.Automation.Runspaces.Runspace]::DefaultRunspace.InstanceId) {
        throw "Build evidence is not owned by a completed, active invocation in this process/runspace."
    }
    if (-not ([IO.Path]::GetFullPath($EvidencePath)).Equals($active.ReceiptPath, [StringComparison]::OrdinalIgnoreCase) -or
        -not ([IO.Path]::GetFullPath($RepositoryRoot)).Equals($active.RepositoryRoot, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Foreign build-evidence path or repository."
    }
    if ((Get-RawFileSha256 $EvidencePath) -cne $active.ReceiptHash) { throw "Build receipt was altered after sealing." }
    $receipt = $active.Receipt
    if ($receipt.allowToolchainDownload -ne [bool]$AllowToolchainDownload) { throw "Build evidence has a different toolchain-download policy." }
    if ($AllowToolchainDownload -and $Arguments -ccontains "-Dorg.gradle.java.installations.auto-download=false") {
        throw "The requested arguments contradict the declared toolchain-download policy."
    }
    Assert-InputIdentity -Expected $active.Inputs -Actual (Get-VerificationInputIdentity -RepositoryRoot $RepositoryRoot -ToolchainFiles $active.ToolchainFiles)
    $requirements = @(Get-RequestedBuildRequirements $Arguments $WorkingDirectory $RepositoryRoot)
    foreach ($requirement in $requirements) {
        if (@($receipt.tasks | Where-Object { $_.task -ceq $requirement.Task -and
                $_.outcome -cin @("EXECUTED", "UP-TO-DATE", "FROM-CACHE") }).Count -eq 0) {
            throw "Canonical build did not cover task $($requirement.Task)."
        }
        $module = @($script:ModuleDefinitions.Keys | Where-Object { $script:ModuleDefinitions[$_].Task -ceq $requirement.Task })
        if ($module.Count -eq 1) {
            $selection = $receipt.selections[$module[0]]
            if ($requirement.Filters.Count -eq 0 -and -not $selection.unfiltered) {
                throw "An unfiltered/FULL test requirement cannot consume a filtered receipt."
            }
            $moduleReports = @($receipt.junit | Where-Object { $_.module -ceq $module[0] })
            foreach ($filter in $requirement.Filters) {
                if (-not $selection.unfiltered -and -not $filter.StartsWith("org.geocedg.", [StringComparison]::Ordinal) -and
                    @($selection.filters | Where-Object { $filter -ceq $_ -or $filter.StartsWith($_ + ".", [StringComparison]::Ordinal) }).Count -eq 0) {
                    throw "Canonical test selection does not cover filter $filter."
                }
                if ($filter -match '[?\[\]]') { throw "Unsupported Gradle filter pattern: $filter" }
                $pattern = "^" + [regex]::Escape($filter).Replace("\*", ".*") + "$"
                $covered = @($moduleReports | Where-Object {
                    $_.class -cmatch $pattern -or @($_.cases | Where-Object { ($_.class + "." + ($_.name -replace '\(.*\)$', '')) -cmatch $pattern }).Count -gt 0
                })
                if ($covered.Count -eq 0) { throw "Requested test filter produced no inspectable current evidence: $filter" }
                foreach ($report in $covered) { Assert-ReportHash $report $RepositoryRoot }
            }
            if ($requirement.Filters.Count -eq 0) {
                foreach ($report in $moduleReports) { Assert-ReportHash $report $RepositoryRoot }
            }
        }
        foreach ($style in @($receipt.checkstyle | Where-Object { $_.task -ceq $requirement.Task })) {
            Assert-ReportHash $style $RepositoryRoot
        }
    }
    [void][IO.Directory]::CreateDirectory((Split-Path -Parent ([IO.Path]::GetFullPath($LogPath))))
    $message = @(
        "CURRENT-RUN EVIDENCE CONSUMPTION; this historical Gradle command was not executed again.",
        "Description: $Description",
        "Run: $($receipt.runId)",
        "Receipt: $EvidencePath",
        "Requested requirements: $($Arguments -join ' ')",
        "Canonical native logs: $(($receipt.nativeRuns.logPath) -join ', ')",
        "The caller must still perform its own original XML/method/count/style assertions."
    ) -join $script:Lf
    [IO.File]::WriteAllText($LogPath, $message + $script:Lf, [Text.UTF8Encoding]::new($false))
    Write-Host "Validated current-run evidence for: $Description"
}

function Close-GeoCeDGBuildEvidence {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [string]$OwnerToken)
    if ($null -eq $script:ActiveEvidence -or $script:ActiveEvidence.Owner -cne $OwnerToken) {
        throw "Cannot close build evidence without its active owning token."
    }
    try {
        Assert-LoadedRuntimeSource
        $active = $script:ActiveEvidence
        if ($active.State -cne "SEALED" -or $active.ProcessId -ne $PID -or
                $active.ModuleIdentity -cne $script:ModuleIdentity -or
                $active.RunspaceId -ne [System.Management.Automation.Runspaces.Runspace]::DefaultRunspace.InstanceId) {
            throw "Cannot complete evidence outside the active producing process/runspace."
        }
        if ((Get-RawFileSha256 $active.ReceiptPath) -cne $active.ReceiptHash) {
            throw "Build receipt changed before completion."
        }
        Assert-InputIdentity -Expected $active.Inputs -Actual (Get-VerificationInputIdentity `
            -RepositoryRoot $active.RepositoryRoot -ToolchainFiles $active.ToolchainFiles)
        foreach ($report in @($active.Receipt.junit) + @($active.Receipt.checkstyle)) {
            Assert-ReportHash -Report $report -RepositoryRoot $active.RepositoryRoot
        }
        foreach ($artifact in $active.Receipt.auditArtifacts) {
            if (-not (Test-Path -LiteralPath $artifact.path -PathType Leaf) -or
                    (Get-RawFileSha256 $artifact.path) -cne $artifact.sha256) {
                throw "Canonical audit artifact changed before completion: $($artifact.path)"
            }
        }
    } finally {
        # Completion validation failure must not leave a reusable live capability.
        $script:ActiveEvidence = $null
    }
}

function Get-GeoCeDGPhaseDefinition {
    [CmdletBinding()]
    param([Parameter(Mandatory)] [string]$Phase)
    Assert-LoadedRuntimeSource
    $key = $Phase.ToUpperInvariant()
    if (-not $script:PhaseVerifiers.Contains($key)) {
        throw "Unknown PHASE '$Phase'. Supported IDs: $($script:PhaseVerifiers.Keys -join ', ')."
    }
    return [pscustomobject]@{ Phase = $key; Verifier = $script:PhaseVerifiers[$key] }
}

function Invoke-GeoCeDGDevVerification {
    [CmdletBinding()]
    param(
        [Parameter(Mandatory)] [string]$RepositoryRoot,
        [Parameter(Mandatory)] [ValidateSet("shared", "desktop")] [string]$Module,
        [Parameter(Mandatory)] [string[]]$TestFilter,
        [Parameter(Mandatory)] [string]$LogDirectory,
        [switch]$AllowToolchainDownload,
        [switch]$KeepBuildOutputs
    )
    Assert-LoadedRuntimeSource
    if ($TestFilter.Count -eq 0 -or @($TestFilter | Where-Object { [string]::IsNullOrWhiteSpace($_) }).Count -gt 0) {
        throw "DEV requires explicit, nonempty test filters; it never infers complete coverage."
    }
    $runRoot = Join-Path ([IO.Path]::GetFullPath($LogDirectory)) ([guid]::NewGuid().ToString("N"))
    Remove-CurrentJUnitReports -RepositoryRoot $RepositoryRoot -Module $Module
    $arguments = Get-TestBuildArguments -Module $Module -Filters $TestFilter -AllowToolchainDownload:$AllowToolchainDownload -KeepBuildOutputs:$KeepBuildOutputs
    $run = Invoke-VerificationNative -FilePath (Join-Path $RepositoryRoot "gradlew.bat") -Arguments $arguments -WorkingDirectory $RepositoryRoot -LogPath (Join-Path $runRoot "dev-gradle.log") -Description "DEV scoped tests; NOT AN ACCEPTANCE GATE"
    $reportFailure = $null
    try {
        $reports = @(Get-JUnitEvidence -RepositoryRoot $RepositoryRoot -Module $Module -ArchiveDirectory (Join-Path $runRoot "junit") -Level DEV)
    } catch { $reportFailure = $_.Exception.Message }
    if ($run.exitCode -ne 0) {
        throw "DEV Gradle failed with exit $($run.exitCode): $($run.logPath). XML inspection: $reportFailure"
    }
    if ($reportFailure) { throw $reportFailure }
    $tasks = @(Get-GradleTaskEvidence $run.text)
    Assert-FreshTestTask -Tasks $tasks -Task $script:ModuleDefinitions[$Module].Task
    foreach ($filter in $TestFilter) {
        # Gradle treats uppercase-first patterns as simple class[/method] names;
        # other patterns match fully qualified names. Only '*' is a wildcard:
        # '?' and brackets remain literal, including parameterized XML names.
        $simpleName = [char]::IsUpper($filter[0])
        $pattern = "\A" + [regex]::Escape($filter).Replace("\*", ".*") + "\z"
        $matching = @($reports | Where-Object {
            @($_.cases | Where-Object {
                $className = [string]$_.class
                if ([string]::IsNullOrWhiteSpace($className)) { return $false }
                if ($simpleName) { $className = $className.Substring($className.LastIndexOf('.') + 1) }
                $reportedName = [string]$_.name
                # Preserve exact display names; also accept the common no-arg
                # method suffix. Never infer Java methods from arbitrary custom
                # display names or erase nonempty parameter/display text.
                $methodName = $reportedName -creplace '\(\)$', ''
                $className -cmatch $pattern -or
                    ($className + "." + $reportedName) -cmatch $pattern -or
                    ($className + "." + $methodName) -cmatch $pattern
            }).Count -gt 0
        })
        if ($matching.Count -eq 0) {
            throw "A requested DEV filter has no inspectable executed class/method evidence: $filter"
        }
    }
    $summary = [ordered]@{
        schemaVersion = 1; level = "DEV"; state = "PASS_SCOPED_NOT_ACCEPTANCE"
        module = $Module; filters = $TestFilter; tests = (@($reports.tests) | Measure-Object -Sum).Sum
        tasks = $tasks; junit = $reports; elapsedSeconds = $run.elapsedSeconds
        nativeExitCode = $run.exitCode; logPath = $run.logPath
        omitted = @("Unselected tests", "Other modules", "Phase/historical/reference gates", "COMPOSED/FULL acceptance")
        authorApproved = $false; selfApproved = $false
    }
    Write-VerificationJson (Join-Path $runRoot "dev-summary.json") $summary
    Write-Host "DEV passed $($summary.tests) selected tests. NOT AN ACCEPTANCE GATE."
    return [pscustomobject]@{ SummaryPath = Join-Path $runRoot "dev-summary.json"; Tests = $summary.tests }
}

Export-ModuleMember -Function ConvertTo-GeoCeDGIncrementalGradleArguments, Assert-GeoCeDGChildVerificationMode, Invoke-GeoCeDGCanonicalBuild, Confirm-GeoCeDGBuildEvidence, Close-GeoCeDGBuildEvidence, Get-GeoCeDGPhaseDefinition, Invoke-GeoCeDGDevVerification, Clear-GeoCeDGIndependentFullTestReports, Assert-GeoCeDGIndependentFullTestOutcome
