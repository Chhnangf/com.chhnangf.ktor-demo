JSON Web Token

技术栈：
RSA (Rivest–Shamir–Adleman)：一种广泛使用的公钥加密系统。
RS256：RSA算法的一个变体，结合SHA-256哈希算法。
JWT：JSON Web Token，一种用于身份验证和信息交换的轻量级安全标准。
Ktor：一个用于构建服务器端应用程序的框架，支持多种编程语言。
OpenSSL：一个开源的加密工具包，用于生成和操作密钥。
Base64URL：一种Base64编码的变体，用于URL安全。

关键要点：
密钥生成：使用OpenSSL生成2048位或更高位数的RSA私钥。
公钥派生：从私钥导出公钥，并保存为PEM格式。
提取公钥属性：提取公钥的模数（modulus）和指数（exponent）。
Base64URL编码：将模数和指数的十六进制表示转换为Base64URL编码。
jwks.json文件：创建并填充包含公钥信息的JSON Web Key Set文件，用于JWT认证。
私钥配置：将私钥配置到Ktor项目中，通常通过环境变量或安全存储实现。

基于window安装OpenSSL(https://slproweb.com/index.html)
配置环境变量

生成RSA私钥和公钥
openssl genpkey -algorithm RSA -out rsa_private_key.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -in rsa_private_key.pem -pubout -out rsa_public_key.pem

使用OpenSSL提取公钥的模数和指数，并转换为Base64URL编码：
openssl pkey -in rsa_public_key.pem -pubin -noout -text


转换指数（Exponent） 到 Base64URL：
echo 010001 | openssl base64 -A -out exponent.b64
e常量为AQAB

转换模数（Modulus） 到 Base64URL：

$hexString = $hexStringWithColons -replace ':', ''

# 原始十六进制字符串
$hexStringWithColons = "00:b3:d5:..52   //模数完整数据

$hexString = $hexStringWithColons -replace ':', '' 去除:符号获得完整十六进制字符
# 将十六进制字符串转换为字节数组
$bytes = [System.Text.Encoding]::UTF8.GetBytes($hexString)

# 将字节数组编码为 Base64 字符串
$base64 = [System.Convert]::ToBase64String($bytes)

# 将 Base64 字符串转换为 Base64URL 字符串
$base64Url = $base64.Replace('+', '-').Replace('/', '_').Replace('=', '')

# 输出结果
Write-Output $base64Url > "H:\GitHub\OpenSSL\modulus.b64"

使用UUID创建kid
$kid = [System.Guid]::NewGuid().ToString()
Write-Output $kid > "H:\GitHub\OpenSSL\kid.b64"

创建jwks.json文件
{
    "keys": [
        {
            "kty": "RSA",
            "e": "AQAB", // 你的Base64URL编码的指数
            "kid": "b51ade2f-eb0e-4644-94d0-7efde6afe0ce", // 你的Key ID
            "n": "MDBiM2Q1MTEwNWRmMWZmYjk5YjhjYzM0MThlZDVhYWU5N2M3NmYyZTlmYWMzZGVjOTljOTI1NmNhNTE1YzY2N2E3ODI2MzZmOTM0OThlNmZjNmJkOGRiOGY0N2VmN2FhY2ExOTk4ODZiNmY3OThhYzUyNmMxZGVkOWU1NDc3MGQ3MDRjYmNjMmE2MzI5YTJkZTVkM2ZhZGYyZDZmZjk0YmNlMzQ0NDA3MDY1MjExNWFlYTFjNmMzZjVmNjE1MzZkNmE3Yzk3MDZkOGIzZjg1MjM1OTE4OWYxMWYxN2Y1NTEzYmZhNjJjNmUyMjYyZTcyODQ2YjNhM2JmNzIyNmZhNDJmMzk3NDRiN2M5Nzg2ODc2NzJhNmIyZWU1YzJmM2Q5NTQ4OWQwY2U4ZjY4MmVhZDIyYmRjNDg4Y2M0ZjA2Y2JjNzU2NTIxMTAwNWU4ZmJhYmFkNjJkMDhiMjAyMmZhOGVjM2U1N2EwYmJhYWFhMzhkZWUzNmEwMTY2NmE2YTQxZDIzNjlmYmI5Y2QyYjc4MDcyYmZiZTQ2ZTAyOGNkMzNhZTkxYzE0ODVlN2YwZGZkMTUyNDlkMzU1MjFlZTIxMGViYWM0N2NjZTc3MzkyOTM1ZWE0YzQyYTc0MjI0MjgzZmM5M2JlNDljNjk0YTExYWRkZDQ5ZTYwNGE3ZWY5NTIyZA" // 你的Base64URL编码的模数
        }
    ]
}

