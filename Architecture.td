graph TD
    subgraph "Clients"
        Client_Web[Web Browser]
        Client_Mobile[Mobile App]
    end

    subgraph "Network & Gateway"
        CDN["Content Delivery Network"]
        Load_Balancer["Load Balancer"]
        API_Gateway["API Gateway"]
    end

    subgraph "Backend Microservices"
        UserService["User Service"]
        TodoListService["TODO List Service"]
        RealtimeService["Real-time Service"]
        MediaService["Media Service"]
    end

    subgraph "Caching Layer"
        UserCache["User Cache"]
        TODOListCache["TODO List Cache"]
    end

    subgraph "Data & Storage Layer"
        UserDB["User DB"]
        TodoDB["TODO List DB"]
        ObjectStorage["Media Object Storage"]
        PubSub["Pub/Sub System"]
    end

    %% Client to Backend Flows
    Client_Web -- "HTTPS Requests" --> Load_Balancer
    Client_Mobile -- "HTTPS Requests" --> Load_Balancer
    Client_Web -- "WebSocket" --> RealtimeService
    Client_Mobile -- "WebSocket" --> RealtimeService
    Client_Web -- "Media Content" --> CDN
    Client_Mobile -- "Media Content" --> CDN

    %% Load balancer to API Gateway
    Load_Balancer -- "Distribute" --> API_Gateway

    %% API Gateway to Services
    API_Gateway -- "Routes To" --> UserService
    API_Gateway -- "Routes To" --> TodoListService
    API_Gateway -- "Routes To" --> MediaService

    %% Service to Data Layer Interactions
    UserService -- "CRUD Users" --> UserCache
    TodoListService -- "CRUD Lists/Items" --> TODOListCache
    UserCache -- "CRUD Users" --> UserDB
    UserDB -- "Sync Data" --> UserCache
    TODOListCache -- "CRUD Lists/Items" --> TodoDB
    TodoDB -- "Sync Data" --> TODOListCache
    MediaService -- "Stores/Retrieves Files" --> ObjectStorage
    MediaService -- "Writes Metadata" --> TodoDB

    %% Real-time Collaboration Flow
    TodoListService -- "Publishes Changes" --> PubSub
    RealtimeService -- "Subscribes to Changes" --> PubSub
    RealtimeService -- "Pushes Updates to Clients" --> Client_Web
    RealtimeService -- "Pushes Updates to Clients" --> Client_Mobile

    %% Media Delivery Flow
    ObjectStorage -- "Serves Files Through" --> CDN