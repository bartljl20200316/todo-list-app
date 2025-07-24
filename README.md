# Real-Time Collaborative TODO List API

This project is a RESTful API for a real-time collaborative TODO list application built with **Java** and **Spring Boot**. It features user authentication, role-based access control, WebSocket integration for real-time updates, and advanced querying capabilities.

---
## Prerequisites

Before you begin, ensure you have the following installed on your system:

* **Java (JDK)**: Version 17 or higher.
* **Apache Maven**: 3.9 or higher. For building the project and managing dependencies.
* **MongoDB**: NoSQL database for storing user and todo list data.
* **Docker**: For running MongoDB instance.
* **API Client**: A client like **Postman** to interact with the API endpoints.

---
## How to Run and Test

### 1. Start mongoDB using docker

```bash
# Download the latest version of mongoDB
docker pull mongodb/mongodb-community-server:latest

# Start the mongoDB instance
docker run --name mongodb -p 27017:27017 -d mongodb/mongodb-community-server:latest
```

### 2. Run the Application

You can run the application directly using the Maven Spring Boot plugin. This will start the web server on `http://localhost:8088`.

```bash
# Navigate to the project's root directory
cd todo-list-app/

# Run the application
mvn spring-boot:run
```

You can configure the port in **application.properties** file:
```bash
#server port
server.port=8088
```

### 3. Run the Test

```bash
## To run all unit and integration tests, clean, and build the project
mvn clean install
```

## API Endpoints

All protected endpoints require a Bearer Token in the Authorization header. If the token expired, it will throw http code 403.
The json file **todolist.postman_collection.json** is the Postman collection for all APIs.

### 1. User Authentication

#### Register a New User
- **Endpoint**: 
```http 
POST /api/auth/signup
```
- **Description**: Create a new user
- **Request Body**:
```json
{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123"
}
```

#### User Login
- **Endpoint**:
```http 
POST /api/auth/login
```
- **Description**: Authenticates a user and returns a JWT token
- **Request Body**:
```json
{
  "username": "testuser",
  "password": "password123"
}
```
- **Response Body**:
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer"
}
```

### 2. Managing Lists and Items
#### Create a New List
- **Endpoint**:
```http 
POST /api/todo-list
```
- **Description**: Creates a new TODO list for the authenticated user
- **Request Body**:
```json
{
  "title": "My Project Plan"
}
```
- **Response Body**:
```json
{
  "id": "6881c3b147e6a681576185b9",
  "title": "My Project Plan",
  "ownerId": "68810fb3eb2cbdd7b7ea423b",
  "items": [],
  "collaborators": []
}
```

#### Get owned or collaborated todo lists
- **Endpoint**:
```http 
GET /api/todo-list
```
- **Description**: Get all lists owned by or shared with the current user.
- **Response Body**:
```json
{
  "totalCount": 1,
  "results": [
    {
      "id": "6881c3b147e6a681576185b9",
      "title": "My Project Plan",
      "ownerId": "68810fb3eb2cbdd7b7ea423b",
      "items": [
        {
          "id": "fb23a87f-8779-440e-9460-8ded76e7a3f2",
          "name": "Item-1",
          "description": "Item 1",
          "dueDate": "2025-09-15",
          "status": "IN_PROGRESS",
          "priority": "LOW",
          "tags": null,
          "completed": false,
          "deleted": true
        }
      ],
      "collaborators": [
        {
          "userId": "6881f3f7d29ad796ce5738cc",
          "permission": "VIEW_ONLY"
        }
      ]
    }
  ]
}
```

#### Add item to List
- **Endpoint**:
```http 
POST /api/todo-list/{listId}/items
```
- **Description**: Adds new tasks to a specific list
- **Request Body**:
```json
[
  {
    "name": "Item-1",
    "description": "Item 1",
    "dueDate": "2025-08-15",
    "priority": "HIGH"
  }
]
```
- **Response Body**:
```json
{
  "id": "6881c3b147e6a681576185b9",
  "title": "My Project Plan",
  "ownerId": "68810fb3eb2cbdd7b7ea423b",
  "items": [
    {
      "id": "fb23a87f-8779-440e-9460-8ded76e7a3f2",
      "name": "Item-1",
      "description": "Item 1",
      "dueDate": "2025-08-15",
      "status": null,
      "priority": "HIGH",
      "tags": null,
      "completed": false,
      "deleted": false
    }
  ],
  "collaborators": []
}
```

#### Update item in List
- **Endpoint**:
```http 
PUT /api/todo-list/{listId}/items
```
- **Description**: Update new tasks in a specific list
- **Request Body**:
```json
[
  {
    "id": "fb23a87f-8779-440e-9460-8ded76e7a3f2",
    "name": "Item-1",
    "description": "New Item 1",
    "dueDate": "2025-09-15",
    "priority": "LOW"
  }
]
```
- **Response Body**:
```json
{
  "id": "6881c3b147e6a681576185b9",
  "title": "My Project Plan",
  "ownerId": "68810fb3eb2cbdd7b7ea423b",
  "items": [
    {
      "id": "fb23a87f-8779-440e-9460-8ded76e7a3f2",
      "name": "Item-1",
      "description": "New Item 1",
      "dueDate": "2025-09-15",
      "status": null,
      "priority": "LOW",
      "tags": null,
      "completed": false,
      "deleted": false
    }
  ],
  "collaborators": []
}
```

#### Delete items in List
- **Endpoint**:
```http 
PUT /api/todo-list/{listId}/items-delete
```
- **Description**: Delete tasks in a specific list. It is soft deletion.
- **Request Body**:
```json
[
  {
    "id": "fb23a87f-8779-440e-9460-8ded76e7a3f2",
    "name": "Item-1",
    "description": "New Item 1",
    "dueDate": "2025-09-15",
    "priority": "LOW"
  }
]
```
- **Response Body**:
```json
{
  "id": "6881c3b147e6a681576185b9",
  "title": "My Project Plan",
  "ownerId": "68810fb3eb2cbdd7b7ea423b",
  "items": [
    {
      "id": "fb23a87f-8779-440e-9460-8ded76e7a3f2",
      "name": "Item-1",
      "description": "New Item 1",
      "dueDate": "2025-09-15",
      "status": "NOT_STARTED",
      "priority": "LOW",
      "tags": null,
      "completed": false,
      "deleted": true
    }
  ],
  "collaborators": []
}
```

### 3. Filtering and Sorting
- **Endpoint**:
```http 
GET /api/todo-list/{listId}/items
```
- **Description**: Retrieves items from a list with optional filtering and sorting query parameters
- **Example URL**: 
```http
GET http://localhost:8088/api/todo-list/60d.../items?status=NOT_STARTED&sortBy=dueDate&sortDir=DESC
```
- **Query Parameters**:
    - name: Task name, optional
    - status: Task status (NOT_STARTED, IN_PROGRESS, COMPLETED), optional
    - dueDate: Task due date, optional
    - sortBy: sort by field name, default value due date
    - sortDir: sort direction (asc, desc), default value is asc
    - includeDeleted: if including deleted tasks, default value is true
- **Response Body**:
```json
{
  "totalCount": 2,
  "results": [
    {
      "id": "94ed1058-a7a7-4b76-9c57-217003b7bcb2",
      "name": "Item-3",
      "description": "Item 3",
      "dueDate": "2025-12-01",
      "status": "NOT_STARTED",
      "priority": "MEDIUM",
      "tags": null,
      "completed": false,
      "deleted": false
    },
    {
      "id": "1a78b9bf-da5b-4d22-8db5-8b90f4a10785",
      "name": "Item-2",
      "description": "Item 2",
      "dueDate": "2025-10-01",
      "status": "NOT_STARTED",
      "priority": "HIGH",
      "tags": null,
      "completed": false,
      "deleted": false
    }
  ]
}
```

### 4. Collaboration

- **Endpoint**:
```http 
POST /api/todo-list/{listId}/share
```
- **Description**: Share a list with another user, granting specific permissions (EDIT, VIEW_ONLY).
- **Request Body**:
```json
{
  "email": "collaborator@example.com",
  "permission": "EDIT"
}
```