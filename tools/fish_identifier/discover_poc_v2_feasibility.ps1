param(
    [Parameter(Mandatory = $true)]
    [string]$OutputPath
)

$ErrorActionPreference = 'Stop'

$classes = @(
    @{ outputIndex = 0; id = 'XTLHUX6xHya0BOisyR6E'; scientific = 'Ocyurus chrysurus'; category = 'Ocyurus_chrysurus'; productionIndex = 22; review = 'ENHANCED_REVIEW' },
    @{ outputIndex = 1; id = 'Hjr9sFSdUEW1RVpR09mV'; scientific = 'Lactophrys quadricornis'; category = 'Acanthostracion_quadricornis'; productionIndex = 7; review = 'ENHANCED_REVIEW' },
    @{ outputIndex = 2; id = 'RO2iuTVLAX11dy3aNgdf'; scientific = 'Haemulon plumieri'; category = 'Haemulon_plumieri'; productionIndex = 15; review = 'ENHANCED_REVIEW' },
    @{ outputIndex = 3; id = 'eBZEv2F3RUvtST6fx0cK'; scientific = 'Lutjanus analis'; category = 'Lutjanus_analis'; productionIndex = 26; review = 'ENHANCED_REVIEW' },
    @{ outputIndex = 4; id = 'qDlhElFdSz5UOHDkU8Pe'; scientific = 'Lactophrys triqueter'; category = 'Lactophrys_triqueter'; productionIndex = 36; review = 'ENHANCED_REVIEW' }
)

function PlainText([object]$value) {
    $text = [string]$value
    $text = [regex]::Replace($text, '<[^>]+>', ' ')
    $text = [System.Net.WebUtility]::HtmlDecode($text)
    return ([regex]::Replace($text, '\s+', ' ')).Trim()
}

function ContextFor([string]$title, [string]$description) {
    $text = ($title + ' ' + $description).ToLowerInvariant()
    if ($text -match 'aquarium|sea life|sealife|oceanarium|zoo') { return 'AQUARIUM' }
    if ($text -match 'catch|caught|fishing|fished|angler|spearfish|landed|market|dock|boat deck|pescad|pêch') { return 'FIELD_CAUGHT_HELD' }
    if ($text -match 'underwater|reef|dive|diving|scuba|natural habitat|marine park|coral') { return 'NATURAL_UNDERWATER' }
    if ($text -match 'fda|specimen|museum|collection|reference|white background') { return 'CONTROLLED_REFERENCE' }
    return 'OTHER'
}

function CorrelationHint([string]$creator, [string]$title) {
    $stem = $title.ToLowerInvariant()
    $stem = $stem -replace '^file:', ''
    $stem = $stem -replace '\.(jpg|jpeg|png|tif|tiff)$', ''
    $stem = $stem -replace '\((cropped|crop|detail|edited|retouched)[^)]*\)', ''
    $stem = $stem -replace '\b(19|20)\d{2}[-_./]?\d{0,2}[-_./]?\d{0,2}\b', ''
    $stem = $stem -replace '\b(dd|dsc|img|image|photo|pic)[-_ ]?\d+\b', ''
    $stem = $stem -replace '\b\d{2,}\b', ''
    $stem = $stem -replace '[^a-z]+', '-'
    $stem = $stem.Trim('-')
    $creatorStem = (($creator.ToLowerInvariant() -replace '[^a-z0-9]+', '-').Trim('-'))
    if (-not $creatorStem) { $creatorStem = 'unknown' }
    if (-not $stem) { $stem = 'untitled' }
    return ($creatorStem + '__' + $stem)
}

$rows = @()
foreach ($class in $classes) {
    $url = 'https://commons.wikimedia.org/w/api.php?action=query&format=json&generator=categorymembers' +
        '&gcmtitle=Category%3A' + $class.category +
        '&gcmtype=file&gcmlimit=500&prop=imageinfo&iiprop=url%7Cextmetadata'
    $response = Invoke-RestMethod $url -Headers @{ 'User-Agent' = 'PescaPR POC v2 feasibility metadata discovery/1.0' }
    if (-not $response.query.pages) {
        Write-Warning "No Commons category files returned for $($class.scientific) using $($class.category)"
        continue
    }
    $pages = @($response.query.pages.psobject.Properties.Value)

    foreach ($page in $pages) {
        $info = $page.imageinfo[0]
        $meta = $info.extmetadata
        $title = [string]$page.title
        $license = PlainText $meta.LicenseShortName.value
        $licenseUrl = PlainText $meta.LicenseUrl.value
        $creator = PlainText $meta.Artist.value
        $credit = PlainText $meta.Credit.value
        $description = PlainText $meta.ImageDescription.value
        $lower = ($title + ' ' + $credit + ' ' + $description).ToLowerInvariant()

        $allowedLicense = $license -match '^(Public domain|CC0|CC BY 2\.0|CC BY 2\.5|CC BY 3\.0|CC BY 4\.0)$'
        $photograph = $title -match '\.(jpg|jpeg|png|tif|tiff)$'
        # Project policy excludes iNaturalist-origin media regardless of the
        # license surfaced by a downstream Commons item.
        $prohibitedOrigin = $lower -match 'inaturalist'
        $nonPhoto = $lower -match 'illustration|drawing|plate|stamp|mapa|range map|distribution map|diagram|logo|food|fillet|meat|recipe|x-ray|radiograph|iconographia|\bfmib\b|skeleton|histoire naturelle|naturgeschichte|photo-engraving|color sketch|watercolou?r|engraving'
        $decision = if (-not $allowedLicense) { 'EXCLUDE_LICENSE' } elseif (-not $photograph) { 'EXCLUDE_NON_PHOTO' } elseif ($prohibitedOrigin) { 'EXCLUDE_PROHIBITED_ORIGIN' } elseif ($nonPhoto) { 'EXCLUDE_NON_FIELD_ARTIFACT' } else { 'PLAUSIBLE' }
        $context = ContextFor $title $description
        $group = CorrelationHint $creator $title

        $rows += [pscustomobject]@{
            # A Commons file may appear in multiple species categories. Keep the
            # candidate record identity unique while preserving sourceItemId so
            # cross-label/source duplication remains detectable.
            candidateId = 'pocv2f-wc-' + $page.pageid + '-c' + $class.outputIndex
            shortlistIndex = $class.outputIndex
            fichaPezId = $class.id
            productionClassifierIndex = $class.productionIndex
            scientificName = $class.scientific
            sourceName = 'Wikimedia Commons'
            sourceItemId = [string]$page.pageid
            sourceItemTitle = $title
            sourcePageUrl = 'https://commons.wikimedia.org/wiki/?curid=' + $page.pageid
            mediaUrl = [string]$info.url
            creator = $creator
            proposedLicense = $license
            licenseUrl = $licenseUrl
            rightsEvidence = 'Commons item extmetadata; exact file-page review still required'
            recordType = if ($photograph) { 'PHOTOGRAPH_CANDIDATE' } else { 'NON_PHOTO' }
            fieldReferenceContext = $context
            sourceSessionGroupHint = $group
            labelReviewBurden = $class.review
            feasibilityDecision = $decision
            rightsStatus = 'PENDING'
            labelStatus = 'PENDING'
            notes = (($credit + ' ' + $description).Trim())
        }
    }
}

# Individually discovered federal records not represented by a unique Commons item.
$rows += [pscustomobject]@{
    candidateId = 'pocv2f-usgs-smooth-trunkfish-2010'; shortlistIndex = 4
    fichaPezId = 'qDlhElFdSz5UOHDkU8Pe'; productionClassifierIndex = 36
    scientificName = 'Lactophrys triqueter'; sourceName = 'U.S. Geological Survey'
    sourceItemId = 'smooth-trunkfish-lactophrys-triqueter-2010'
    sourceItemTitle = 'Smooth Trunkfish (Lactophrys triqueter)'
    sourcePageUrl = 'https://www.usgs.gov/media/images/smooth-trunkfish-lactophrys-triqueter'
    mediaUrl = ''; creator = 'Caroline Rogers'; proposedLicense = 'Public domain'
    licenseUrl = 'https://www.usgs.gov/information-policies-and-instructions/copyrights-and-credits'
    rightsEvidence = 'The individual USGS media page explicitly states Sources/Usage: Public Domain.'
    recordType = 'PHOTOGRAPH_CANDIDATE'; fieldReferenceContext = 'NATURAL_UNDERWATER'
    sourceSessionGroupHint = 'usgs-caroline-rogers-smooth-trunkfish-2010'
    labelReviewBurden = 'ENHANCED_REVIEW'; feasibilityDecision = 'PLAUSIBLE'
    rightsStatus = 'PENDING'; labelStatus = 'PENDING'
    notes = 'Item-level public-domain evidence is clear; image identity and independence still require visual adjudication.'
}
$rows += [pscustomobject]@{
    candidateId = 'pocv2f-fda-248'; shortlistIndex = 3
    fichaPezId = 'eBZEv2F3RUvtST6fx0cK'; productionClassifierIndex = 26
    scientificName = 'Lutjanus analis'; sourceName = 'U.S. Food and Drug Administration'
    sourceItemId = 'FDA-248'; sourceItemTitle = 'Reference Standard Sequence Library: Lutjanus analis'
    sourcePageUrl = 'https://hfpappexternal.fda.gov/scripts/fdcc/index.cfm?id=FDA_248&set=RSSL_seafood_barcode_data'
    mediaUrl = ''; creator = ''; proposedLicense = 'Public domain — verification pending'
    licenseUrl = ''; rightsEvidence = 'Federal FDA item page; exact image authorship/public-domain status is not stated on the item page.'
    recordType = 'PHOTOGRAPH_CANDIDATE'; fieldReferenceContext = 'CONTROLLED_REFERENCE'
    sourceSessionGroupHint = 'fda-rssl-fda-248'
    labelReviewBurden = 'SPECIALIST_REQUIRED'; feasibilityDecision = 'RIGHTS_PENDING'
    rightsStatus = 'PENDING'; labelStatus = 'PENDING'
    notes = 'Real federal reference record, but it is not counted as likely rights-usable until item-level image authorship is established.'
}

$parent = Split-Path -Parent $OutputPath
if ($parent -and -not (Test-Path -LiteralPath $parent)) {
    New-Item -ItemType Directory -Path $parent -Force | Out-Null
}
$rows | Sort-Object shortlistIndex, sourceItemId | Export-Csv -LiteralPath $OutputPath -NoTypeInformation -Encoding utf8
Write-Output "metadata rows=$($rows.Count) plausible=$(@($rows | Where-Object feasibilityDecision -eq 'PLAUSIBLE').Count) output=$OutputPath"
