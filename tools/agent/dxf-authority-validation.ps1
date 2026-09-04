#requires -Version 7.2
# G9X1 still scans its complete productive perimeter. A later application view
# factory is presentation wiring, not an export source; only its exact approved
# declaration and import may be projected out of the historical authority scan.
function Assert-GeoCeDGDxfSourceAuthority {
    param(
        [Parameter(Mandatory)] [string]$RelativePath,
        [Parameter(Mandatory)] [string]$Content
    )

    $scan = $Content.Replace("`r`n", "`n")
    if ($RelativePath -ceq 'source/desktop/desktop/src/main/java/org/geocedg/desktop/AppGeoCeDG.java' -and
        $scan.Contains('EuclidianView')) {
        $viewImport = "import org.geogebra.common.euclidian.EuclidianView;`n"
        $viewFactory = @'
	@Override
	protected EuclidianView newEuclidianView(boolean[] showAxes, boolean showGrid) {
		return new GeoCeDGEuclidianView(getEuclidianController(), showAxes,
				showGrid, 1, getSettings().getEuclidian(1));
	}
'@
        $viewFactory = $viewFactory.Replace("`r`n", "`n")
        foreach ($fragment in @($viewImport, $viewFactory)) {
            if ([regex]::Matches($scan, [regex]::Escape($fragment)).Count -ne 1) {
                throw "$RelativePath lacks exactly one authenticated presentation-only view factory/import."
            }
            $scan = $scan.Replace($fragment, '')
        }
    }

    # These are the original G9X1 prohibitions, unchanged. The remaining App
    # source, all export seams and every other productive Java path are scanned.
    foreach ($forbidden in @(
            'evaluateForRender', 'LocusRenderCache', 'myPointList',
            'EuclidianView', 'System.nanoTime', 'System.currentTimeMillis',
            'ThreadLocalRandom', 'Math.random', 'parallelStream')) {
        if ($scan.Contains($forbidden)) {
            throw "$RelativePath uses forbidden G9X1 authority: $forbidden"
        }
    }
    if ($scan -match '(?i)ProcessBuilder\s*\([^\)]*["'']git["'']' -or
        $scan -match '(?i)Runtime\.getRuntime\(\)\.exec\s*\([^\)]*git') {
        throw "$RelativePath invokes Git at export runtime."
    }
}
