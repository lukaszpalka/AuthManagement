# AuthFilter

<p>AuthFilter is a non-production Java application developed to showcase authentication and authorization mechanisms
in Java with Spring Boot. It focuses on generating and validation of JWT tokens, creating custom filters,
integrating them into the Spring Security filter chain, and securing some endpoints by requiring a bearer token.</p>

<p>The application features user registration, login, and a refresh token mechanism. By extracting user roles from tokens,
AuthFilter ensures proper access control based on permissions.</p>
<br>

#### Stack

* Java 17
* Spring Boot 3.3.4
* Maven 4.0.0
* jUnit 5.10.2
* PostgreSQL 13
* Docker compose
* Flyway
* Auth0 JWT

---
# How to Run
To run the application, follow these steps:
* Clone this repository.
* Make sure you are using JDK 8 and Maven 4.0.0 or higher.
* Define the environment variables `RSA_PRIVATE_KEY` and `RSA_PUBLIC_KEY` that store the RSA keys required for secure token operations. You can generate them using OpenSSL. 
* Set database details by editing the `application.properties` and `docker-compose.yml` files.
* Edit `flyway.conf` and Flyway migration file `V1_1__first_data.sql` to suit your needs.
* Start Docker container by command `docker compose up -d`.

### Build the project
In your terminal navigate to the root directory of the project and run following Maven command:
```
mvn clean install
```

### Run the application
```
mvn spring-boot:run
```

Alternatively, you can run the app in your IDE.

---

# Service
The application is just a simple REST service. You can perform some operations:
* Get the list of all registered users
* Sign up / activate user / sign in
* Refresh access token
* Update user's roles
* Delete user (super admin role needed)
* Get, add, modify or delete product (admin role needed)
* Get, add, modify or delete category (admin role needed)

#### To do any of these, follow the steps below:

## User service
### Get all users

```
GET /user/all

Postman:
GET /user/all

curl:
curl http://localhost:8080/user/all

Returns: List of user dtos (List<UserDto>)
HTTP Response Code: 200 (OK)
```

### Sign up
````
POST /signup
Content-Type: application/json
Request Body: JSON with required data from UserDto object (String username, String password, String email, Set<Roles> roles)

Postman:
POST /signup
{
    "username": "super_adm",
    "password": "admin",
    "email": "super@admin",
    "roles": [
        "USER",
        "ADMIN",
        "SUPER_ADMIN"
    ]
}

curl:
curl -X POST http://localhost:8080/signup \
-H "Content-Type: application/json" \
-d '{
    "username": "super_adm",
    "password": "admin",
    "email": "super@admin",
    "roles": [
        "USER",
        "ADMIN",
        "SUPER_ADMIN"
    ]
}'

HTTP Response Code: 201 (Created)
````

### Activate account
```
PATCH /user/activate/{id}
replace "{id}" with desired user id

Postman:
PATCH /user/activate/{id}

curl:
curl -X PATCH http://localhost:8080/user/activate/{id}

HTTP Response Code: 200 (OK) if activated, 404 (Not Found) if user does not exist
```

### Sign in

```
PATCH /signin
Content-Type: application/json
Request Body: JSON with credentials (String username, String password)

Postman:
PATCH /signin
{
    "username": "super_adm",
    "password": "admin"
}

curl:
curl -X PATCH http://localhost:8080/signin \
    -H "Content-Type: application/json" \
    -d '{
        "username": "super_adm",
        "password": "admin"
    }'

Returns: object LoginResponseDto(String accessToken, String refreshToken)
HTTP Response Code: 200 (OK) if signed in, 
                    400 (Bad Request) if some of credentials are missing, 
                    403 (Forbidden) if user is inactive or credentials are wrong 
```

### Refresh access token
```
PATCH /refreshtoken
Authorization: Bearer {refreshToken}
replace "{refreshToken}" with refresh token returned after signing in

Postman:
PATCH /refreshtoken

curl:
curl -X PATCH http://localhost:8080/refreshtoken \
    -H "Authorization: Bearer {refreshToken}"

Returns: object LoginResponseDto(String accessToken, String refreshToken)
HTTP Response Code: 200 (OK) if updated, 
                    400 (Bad Request) if bearer not provided,
                    401 (Unauthorized) if refresh token is expired or incorrect                 
```

### Update roles
```
PATCH /user/roles
Content-Type: application/json
Request Body: JSON with required data from UserDto object (String username, Set<Roles> roles)

Postman:
PATCH /user/roles
{
    "username": "user",
    "roles": [
            "USER",
            "ADMIN"
        ]
}

curl:
curl -X PATCH http://localhost:8080/user/roles \
    -H "Content-Type: application/json" \
    -d '{
        "username": "user",
        "roles": [
            "USER",
            "ADMIN"
        ]
    }'

HTTP Response Code: 200 (OK) if updated, 
                    400 (Bad Request) if some of credentials are missing
```

## Action service

Due to custom filter settings, every endpoint in the ActionController requires a bearer token, which should be provided in the "Authorization" request header.

The main purpose of this setup is to demonstrate the custom filtering mechanism in relation to roles. As a result, each action just validates accessibility basing on the data provided in the JWT token.

### Delete user
```
DELETE /action/user/delete/{id}
replace "{id}" with desired user id

Authorization header: Bearer {accessToken}
replace "{accessToken}" with access token returned after signing in or token refreshing

Postman:
DELETE /action/user/delete/{id}

curl:
curl -X DELETE http://localhost:8080/action/user/delete/{id} \
     -H "Authorization: Bearer {accessToken}"

HTTP Response Code: 204 (No Content) if user deleted from database,
                    401 (Unauthorized) if role requirement not met,
                    404 (Not Found) if user does not exist
```

### Product
```
GET /action/product/get
Authorization header: Bearer {accessToken}
replace "{accessToken}" with access token returned after signing in or token refreshing

Postman:
GET /action/product/get

curl:
curl http://localhost:8080/action/product/get

Returns: String "Product"
HTTP Response Code: 200 (OK),
                    401 (Unauthorized) if role requirement not met
```

```
POST /action/product/add
PATCH /action/product/modify
DELETE /action/product/delete

Authorization header: Bearer {accessToken}
replace "{accessToken}" with access token returned after signing in or token refreshing

Postman:
POST /action/product/add
PATCH /action/product/modify
DELETE /action/product/delete

curl:
curl -X POST http://localhost:8080/action/product/add \
    -H "Authorization: Bearer {accessToken}"
curl -X PATCH http://localhost:8080/action/product/modify \
    -H "Authorization: Bearer {accessToken}"
curl -X DELETE http://localhost:8080/action/product/delete \
    -H "Authorization: Bearer {accessToken}"

HTTP Response Code: 200 (OK),
                    401 (Unauthorized) if role requirement not met
```

### Category
```
GET /action/category/get
Authorization header: Bearer {accessToken}
replace "{accessToken}" with access token returned after signing in or token refreshing

Postman:
GET /action/category/get

curl:
curl http://localhost:8080/action/category/get

Returns: String "Category"
HTTP Response Code: 200 (OK),
                    401 (Unauthorized) if role requirement not met
```

```
POST /action/category/add
PATCH /action/category/modify
DELETE /action/category/delete

Authorization header: Bearer {accessToken}
replace "{accessToken}" with access token returned after signing in or token refreshing

Postman:
POST /action/category/add
PATCH /action/category/modify
DELETE /action/category/delete

curl:
curl -X POST http://localhost:8080/action/category/add \
    -H "Authorization: Bearer {accessToken}"
curl -X PATCH http://localhost:8080/action/category/modify \
    -H "Authorization: Bearer {accessToken}"
curl -X DELETE http://localhost:8080/action/category/delete \
    -H "Authorization: Bearer {accessToken}"

HTTP Response Code: 200 (OK),
                    401 (Unauthorized) if role requirement not met
```
