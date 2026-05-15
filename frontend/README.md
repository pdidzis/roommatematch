# RoommateMatch Frontend

React.js frontend for the RoommateMatch platform.

## Tech Stack
- React 18
- Material UI v5
- React Router v6
- Axios
- Socket.io / STOMP WebSocket
- React Hot Toast

## Getting Started

### Prerequisites
- Node.js 18+
- Backend running on http://localhost:8080

### Installation
```bash
cd frontend
npm install
```

### Running
```bash
npm start
```

Opens on http://localhost:3000

## Features
- JWT Authentication with role-based access
- Real-time chat with WebSocket
- Intelligent roommate matching algorithm
- Lifestyle compatibility scoring
- Roommate journey flow
- Partner offers and discounts
- Admin dashboard
- In-app notifications
- Mobile responsive design

## User Roles
- **TENANT**: Browse matches, chat, find listings
- **LANDLORD**: List properties, chat with tenants
- **PARTNER**: Create discount offers
- **ADMIN**: Verify listings, view statistics
