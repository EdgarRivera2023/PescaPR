param(
    [Parameter(Mandatory = $true)]
    [string]$Path
)

$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing

$resolvedPath = (Resolve-Path -LiteralPath $Path).Path
$source = [System.Drawing.Image]::FromFile($resolvedPath)

try {
    $size = 32
    $bitmap = New-Object System.Drawing.Bitmap $size, $size
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)

    try {
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        $graphics.DrawImage($source, 0, 0, $size, $size)
    }
    finally {
        $graphics.Dispose()
    }

    $pixels = New-Object 'double[,]' $size, $size
    for ($y = 0; $y -lt $size; $y++) {
        for ($x = 0; $x -lt $size; $x++) {
            $color = $bitmap.GetPixel($x, $y)
            $pixels[$x, $y] = (0.299 * $color.R) + (0.587 * $color.G) + (0.114 * $color.B)
        }
    }

    $coefficients = New-Object System.Collections.Generic.List[double]
    for ($v = 0; $v -lt 8; $v++) {
        for ($u = 0; $u -lt 8; $u++) {
            $sum = 0.0
            for ($y = 0; $y -lt $size; $y++) {
                $cosY = [Math]::Cos((([Math]::PI * (2 * $y + 1) * $v) / (2 * $size)))
                for ($x = 0; $x -lt $size; $x++) {
                    $cosX = [Math]::Cos((([Math]::PI * (2 * $x + 1) * $u) / (2 * $size)))
                    $sum += $pixels[$x, $y] * $cosX * $cosY
                }
            }
            $coefficients.Add($sum)
        }
    }

    $sorted = @($coefficients | Sort-Object)
    $median = ($sorted[31] + $sorted[32]) / 2.0
    [UInt64]$hash = 0
    for ($index = 0; $index -lt 64; $index++) {
        if ($coefficients[$index] -gt $median) {
            $hash = $hash -bor ([UInt64]1 -shl (63 - $index))
        }
    }

    '{0:x16}' -f $hash
}
finally {
    if ($null -ne $bitmap) {
        $bitmap.Dispose()
    }
    $source.Dispose()
}
