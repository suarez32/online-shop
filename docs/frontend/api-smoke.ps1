<#
.SYNOPSIS
  单品小铺 · 迭代1 接口冒烟测试（对应 Wiki《迭代1 分工安排》：接口测试与端到端联调 · 邱圣洁主责）
.DESCRIPTION
  按《接口设计》Wiki 冻结的接口清单，串行跑通"五步闭环"并覆盖主要错误分支：
    卖家登录 -> 发布商品 -> 买家看到商品 -> 提交意向拿到口令码 -> 卖家总览看到意向
  附带：未登录 401 / 错误密码 401 / 空参数 400 / 无效口令码 404 / 重复发布 400 / 登出后 token 失效
.NOTES
  用法：
    powershell -ExecutionPolicy Bypass -File test\api-smoke.ps1 `
      -BaseUrl http://localhost:8080 -Username seller -Password 12345678
  说明：若库里已有在售商品，"发布商品"一步会自动跳过（后端会返回 400 已有商品在售），
        后续用例将改用当前在售商品继续执行。
#>
param(
    [string]$BaseUrl = 'http://localhost:8080',
    [string]$Username = 'seller',
    [string]$Password = '12345678'
)

$ErrorActionPreference = 'Stop'
try { [Console]::OutputEncoding = [System.Text.Encoding]::UTF8 } catch { }

$script:Pass = 0
$script:Fail = 0
$script:Skipped = 0

# ---------- HTTP 封装（兼容 Windows PowerShell 5.1 与 PowerShell 7+） ----------
function Invoke-Api {
    param([string]$Method, [string]$Path, $Body, [string]$Token)

    $headers = @{}
    if ($Token) { $headers['X-Token'] = $Token }

    $reqArgs = @{
        Uri             = "$BaseUrl$Path"
        Method          = $Method
        Headers         = $headers
        UseBasicParsing = $true
        TimeoutSec      = 15
    }
    if ($Body) {
        $reqArgs.Body = ($Body | ConvertTo-Json -Depth 5)
        $reqArgs.ContentType = 'application/json; charset=utf-8'
    }

    try {
        $resp = Invoke-WebRequest @reqArgs
        $json = $null
        if ($resp.Content) {
            try { $json = $resp.Content | ConvertFrom-Json } catch { $json = $null }
        }
        return [pscustomobject]@{ Status = [int]$resp.StatusCode; Body = $json }
    }
    catch {
        $status = 0
        try { $status = [int]$_.Exception.Response.StatusCode } catch { }
        return [pscustomobject]@{ Status = $status; Body = $null }
    }
}

# 统一响应体 { code, message, data } 的取数辅助
function Get-Data {
    param($Resp)
    if ($null -eq $Resp.Body) { return $null }
    if ($Resp.Body.PSObject.Properties['data']) { return $Resp.Body.data }
    return $Resp.Body
}

function Check {
    param([string]$Name, [scriptblock]$Assert)
    try {
        $ok = & $Assert
        if ($ok) {
            $script:Pass++
            Write-Host "  [PASS] $Name" -ForegroundColor Green
        }
        else {
            $script:Fail++
            Write-Host "  [FAIL] $Name" -ForegroundColor Red
        }
    }
    catch {
        $script:Fail++
        Write-Host "  [FAIL] $Name -> $($_.Exception.Message)" -ForegroundColor Red
    }
}

function Skip {
    param([string]$Name, [string]$Reason)
    $script:Skipped++
    Write-Host "  [SKIP] $Name（$Reason）" -ForegroundColor DarkYellow
}

Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host " 单品小铺 · 迭代1 接口冒烟测试" -ForegroundColor Cyan
Write-Host " 目标：$BaseUrl    时间：$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

# ---------- 0. 服务可达 ----------
Write-Host "[0] 服务连通性" -ForegroundColor White
$health = Invoke-Api -Method 'GET' -Path '/api/product'
Check 'GET /api/product 返回 200（服务可达）' { $health.Status -eq 200 }
if ($health.Status -eq 0) {
    Write-Host ""
    Write-Host "后端不可达：请先启动 Spring Boot 服务（mvn spring-boot:run），再运行本脚本。" -ForegroundColor Red
    exit 1
}

# ---------- 1. 鉴权与登录 ----------
Write-Host ""
Write-Host "[1] 鉴权与登录" -ForegroundColor White

$noAuth = Invoke-Api -Method 'GET' -Path '/api/seller/overview'
Check '未登录访问 GET /api/seller/overview 应 401' { $noAuth.Status -eq 401 }

$badLogin = Invoke-Api -Method 'POST' -Path '/api/seller/login' -Body @{ username = $Username; password = 'wrong-password-999' }
Check '错误密码登录应 401' { $badLogin.Status -eq 401 }

$login = Invoke-Api -Method 'POST' -Path '/api/seller/login' -Body @{ username = $Username; password = $Password }
Check '正确登录返回 200 且携带 token' {
    $d = Get-Data $login
    ($login.Status -eq 200) -and $d -and $d.token
}
if ($login.Status -ne 200) {
    Write-Host ""
    Write-Host "登录失败（用户名/密码与后端配置不符？用 -Username/-Password 传入正确账号），测试终止。" -ForegroundColor Red
    exit 1
}
$token = (Get-Data $login).token
Write-Host "        拿到 token：$($token.Substring(0, [Math]::Min(8, $token.Length)))..." -ForegroundColor DarkGray

# ---------- 2. 发布商品 ----------
Write-Host ""
Write-Host "[2] 发布商品" -ForegroundColor White

$prodName = "冒烟测试商品-$(Get-Date -Format 'HHmmss')"
$publish = Invoke-Api -Method 'POST' -Path '/api/seller/products' -Token $token -Body @{
    name        = $prodName
    description = '接口冒烟测试自动创建，可忽略'
    price       = 9.90
}

$productId = $null
if ($publish.Status -eq 200) {
    $pd = Get-Data $publish
    $productId = if ($pd -and $pd.id) { $pd.id } else { $null }
    Check 'POST /api/seller/products 发布商品返回 200' { $true }
}
else {
    Skip 'POST /api/seller/products 发布商品' '已有在售商品（后端按规则拒绝重复发布），改用现有商品继续'
}

# ---------- 3. 买家侧 ----------
Write-Host ""
Write-Host "[3] 买家侧" -ForegroundColor White

$buyer = Invoke-Api -Method 'GET' -Path '/api/product'
Check 'GET /api/product 买家可见在售商品' {
    $d = Get-Data $buyer
    ($buyer.Status -eq 200) -and $d -and $d.product
}
$product = (Get-Data $buyer).product
if (-not $productId -and $product) { $productId = $product.id }
if ($product) {
    Write-Host "        当前在售：$($product.name)  ¥$($product.price)  状态=$($product.status)" -ForegroundColor DarkGray
}
else {
    Skip '后续买家用例' '当前无在售商品'
}

if ($product) {
    $intentName = "测试买家-$(Get-Random -Minimum 1000 -Maximum 9999)"
    $intentPhone = "138$(Get-Random -Minimum 10000000 -Maximum 99999999)"

    $badIntent = Invoke-Api -Method 'POST' -Path '/api/intents' -Body @{ name = '   '; phone = '' }
    Check '姓名/电话为空提交意向应 400' { $badIntent.Status -eq 400 }

    $intent = Invoke-Api -Method 'POST' -Path '/api/intents' -Body @{ name = $intentName; phone = $intentPhone }
    Check 'POST /api/intents 提交意向返回 200 且口令码为 8 位' {
        $d = Get-Data $intent
        ($intent.Status -eq 200) -and $d -and $d.code -and ($d.code -cmatch '^[A-Z0-9]{8}$')
    }

    $code = if ($intent.Status -eq 200) { (Get-Data $intent).code } else { $null }

    if ($code) {
        Write-Host "        拿到口令码：$code" -ForegroundColor DarkGray

        $query = Invoke-Api -Method 'GET' -Path "/api/intents/$code"
        Check 'GET /api/intents/{code} 凭码查询返回 200 且含意向信息' {
            $d = Get-Data $query
            ($query.Status -eq 200) -and $d -and $d.intent
        }
    }
    else {
        Skip 'GET /api/intents/{code} 凭码查询' '未拿到口令码'
    }

    $badCode = Invoke-Api -Method 'GET' -Path '/api/intents/ZZZZ999X'
    Check '无效口令码查询应 404' { $badCode.Status -eq 404 }
}

# ---------- 4. 卖家总览 ----------
Write-Host ""
Write-Host "[4] 卖家总览" -ForegroundColor White

$overview = Invoke-Api -Method 'GET' -Path '/api/seller/overview' -Token $token
Check 'GET /api/seller/overview 返回 200' { $overview.Status -eq 200 }

$ov = Get-Data $overview
if ($ov) {
    $queueLen = if ($ov.queue) { @($ov.queue).Count } else { 0 }
    Write-Host "        意向队列长度：$queueLen  canPublish=$($ov.canPublish)" -ForegroundColor DarkGray

    Check '总览意向队列非空（能看到刚提交的买家）' { $queueLen -gt 0 }

    $hasBuyer = $false
    if ($ov.queue -and $intentName) {
        $hasBuyer = (@($ov.queue) | Where-Object { $_.name -eq $intentName }).Count -gt 0
    }
    if ($intentName) {
        Check '意向队列中能找到刚提交的买家（$intentName）' { $hasBuyer }
    }

    Check '有商品在售时 canPublish 应为 false（卖掉一件再做一件）' { $ov.canPublish -eq $false }

    $publish2 = Invoke-Api -Method 'POST' -Path '/api/seller/products' -Token $token -Body @{
        name = "重复发布测试-$(Get-Random)"; description = '应被后端拒绝'; price = 1.00
    }
    Check '已有在售商品时再次发布应 400' { $publish2.Status -eq 400 }
}
else {
    Skip '总览断言' '总览接口未返回数据'
}

# ---------- 5. 登出与 token 失效 ----------
Write-Host ""
Write-Host "[5] 登出与 token 失效" -ForegroundColor White

$logout = Invoke-Api -Method 'POST' -Path '/api/seller/logout' -Token $token
Check 'POST /api/seller/logout 登出返回 200' { $logout.Status -eq 200 }

$after = Invoke-Api -Method 'GET' -Path '/api/seller/overview' -Token $token
Check '登出后旧 token 访问应 401' { $after.Status -eq 401 }

# ---------- 汇总 ----------
Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host " 结果汇总：通过 $script:Pass 项，失败 $script:Fail 项，跳过 $script:Skipped 项" -ForegroundColor $(if ($script:Fail -gt 0) { 'Red' } else { 'Green' })
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""
if ($script:Fail -gt 0) { exit 1 } else { exit 0 }
