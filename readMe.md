# Redis Kullanarak Kullanıcı Oturum Yönetimi
Bu proje, Redis kullanarak kullanıcı girişi ve oturum yönetimi işlemlerini
gerçekleştirmektedir. Kullanıcı girişi işlemi başarılı bir şekilde gerçekleştiğinde, kullanıcıya
bir oturum anahtarı verilir ve bu anahtar ile oturum yönetimi işlemleri gerçekleştirilir. Oturum
anahtarı, Redis veritabanında saklanır ve kullanıcı çıkış yapana ya da oturum süresi dolana kadar geçerlidir.

## Projede Kullanılan Teknolojiler
- Java - Spring Boot
- Redis
- Maven
- MySQL
- Docker

## Projeyi Çalıştırma
Proje, Docker üzerinde çalıştırılmıştır. Docker üzerinde çalıştırmak için aşağıdaki adımları takip edebilirsiniz.

Docker compose dosyasını çalıştırarak MySQL ve Redis veritabanlarını ayağa kaldırın.
```bash
docker-compose up
```
Proje dosyasını çalıştırın.
```bash
mvn spring-boot:run
```
Proje, varsayılan olarak 8080 portunda çalışmaktadır. Tarayıcınızdan http://localhost:8080 adresine giderek projeyi test edebilirsiniz.



## Redis ile Oturum Yönetimi
Redis, oturum yönetimi işlemleri için oldukça kullanışlı bir araçtır. Redis, hafızada veri saklama işlemlerini gerçekleştirdiği için, oturum yönetimi işlemleri oldukça hızlı bir şekilde gerçekleştirilebilir. Bu proje, Redis kullanarak kullanıcı girişi ve oturum yönetimi işlemlerini gerçekleştirmektedir.
Kullanıcı giriş yaptığı anda Redis veritabanına token ve refresh token anahtarları ile birlikte kullanıcı adı kaydedilir. Kullanıcı çıkış yaptığında ise Redis veritabanından ilgili token ve refresh token anahtarları silinir.

İlk kaydedilen token anahtarı, kullanıcıya verilen oturum anahtarıdır. Bu anahtar ile kullanıcı, oturum yönetimi işlemlerini gerçekleştirir. Token anahtarı, kullanıcı çıkış yapana kadar geçerlidir. Token anahtarı süresi dolduğunda, kullanıcıya yeni bir token anahtarı verilir ve eski token anahtarı silinir. Bu işlem, kullanıcının oturumunun sürekli açık kalmasını sağlar.
### Token ve Refresh Token Anahtarları Redis Veritabanına Kaydetme
```java
 public void saveToken(String token, String username){
        redisTemplate.opsForValue().set(token,username, Duration.ofMinutes(5)); 
    } 
```
Yukarıdaki kod bloğunda token anahtarı ve kullanıcı adı Redis veritabanına kaydedilir. Token anahtarı, 5 dakika süreyle geçerli olacaktır. Token süresi dolduğunda, kullanıcıya yeni bir token anahtarı verilir ve eski token anahtarı silinir.

Benzer bir kod bloğu refreshToken için de uygulanır.

### Token ve Refresh Token Anahtarlarını Redis Veritabanından Silme
Fonksiyona verilen token anahtarı, Redis veritabanından silinir.
```java
 public void invalidateToken(String token){
        redisTemplate.opsForValue().getAndDelete(token);
    }
```

### Token ve Refresh Token Anahtarlarının Geçerlilik Durumunu Kontrol Etme
Fonksiyona verilen token anahtarı, Redis veritabanında var mı yok mu kontrol edilir.
```java
 public boolean isTokenValid(String refToken){
    return redisTemplate.opsForValue().get(refToken) != null;
}
```

### Kullanıcı Girişi
Kullanıcı giriş işlemini gerçekleştirdiğinde token ve refresh token anahtarları oluşturulup Redis veritabanına kaydedilir.
```java
 public Token loginUser(UserLoginDto user) {
        User userFromDb = (User) userRepository.findByUsername(user.getUsername()).orElse(null);
        if (userFromDb != null) {
            String hashedPassword = hashPassword(user.getPassword());
            if (userFromDb.getPassword().equals(hashedPassword)) {
                String token = generateToken();
                String refToken = generateToken();
                tokenService.refToken(refToken, userFromDb.getUsername());
                tokenService.saveToken(token, userFromDb.getUsername());
                return new Token(token, userFromDb.getUsername(), refToken);
            }
        }

        return null;
    } 
   ```
Kullanıcı giriş işlemini gerçekleştirdiğinde token ve refresh token anahtarları oluşturulup Redis veritabanına kaydedilir.

### Refresh Token İşlemi

```java
public Token refreshToken(Token oldToken){
    boolean isValid = tokenService.isTokenValid(oldToken.getRefreshToken());
    if(isValid){
        String token = generateToken();
        String newRefToken = generateToken();
        Token newToken = new Token(token, oldToken.getUsername(), newRefToken);
        newToken.setToken(token);
        newToken.setRefreshToken(newRefToken);
        tokenService.saveToken(newToken.getToken(), oldToken.getUsername());
        tokenService.refToken(newToken.getRefreshToken(), oldToken.getUsername());
        tokenService.invalidateToken(oldToken.getToken());
        tokenService.invalidateToken(oldToken.getRefreshToken());
        return newToken;
    }
    return null;
}   
```
Kullanıcı giriş yaptıktan sonra token süresi dolduğunda, refreshToken ile yeni bir token anahtarı oluşturulur. Eski token ve refreshToken anahtarları silinir ve yeni token anahtarı Redis veritabanına kaydedilir.