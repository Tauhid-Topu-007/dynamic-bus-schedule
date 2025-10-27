const express = require('express');
const cors = require('cors');
require('dotenv').config();

const db = require('./config/database');
const authRoutes = require('./routes/auth');
const userRoutes = require('./routes/users');
const busRoutes = require('./routes/buses');
const scheduleRoutes = require('./routes/schedules');
const adminRoutes = require('./routes/admin');

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Request logging middleware
app.use((req, res, next) => {
    console.log(`${new Date().toISOString()} - ${req.method} ${req.path} - IP: ${req.ip}`);
    next();
});

// Routes
app.use('/api/auth', authRoutes);
app.use('/api/users', userRoutes);
app.use('/api/buses', busRoutes);
app.use('/api/schedules', scheduleRoutes);
app.use('/api/admin', adminRoutes);

// Health check route
app.get('/api/health', (req, res) => {
    res.json({
        success: true,
        message: 'Dynamic Bus Schedule API is running',
        timestamp: new Date().toISOString(),
        environment: process.env.NODE_ENV || 'development',
        version: '1.0.0'
    });
});

// API documentation route
app.get('/api', (req, res) => {
    res.json({
        success: true,
        message: 'Dynamic Bus Schedule API',
        version: '1.0.0',
        endpoints: {
            auth: {
                'POST /api/auth/register': 'User registration',
                'POST /api/auth/login': 'User login',
                'GET /api/auth/me': 'Get current user'
            },
            users: {
                'GET /api/users': 'Get all users (admin only)',
                'GET /api/users/drivers': 'Get all drivers',
                'GET /api/users/clients': 'Get all clients (admin only)',
                'GET /api/users/:id': 'Get user by ID',
                'PUT /api/users/:id': 'Update user (admin only)',
                'DELETE /api/users/:id': 'Delete user (admin only)'
            },
            buses: {
                'POST /api/buses': 'Create bus (admin only)',
                'GET /api/buses': 'Get all buses',
                'GET /api/buses/:id': 'Get bus by ID',
                'PUT /api/buses/:id': 'Update bus (admin only)',
                'PATCH /api/buses/:id/status': 'Update bus status (admin only)',
                'DELETE /api/buses/:id': 'Delete bus (admin only)'
            },
            schedules: {
                'POST /api/schedules': 'Create schedule (admin only)',
                'GET /api/schedules': 'Get all schedules (with filters)',
                'GET /api/schedules/:id': 'Get schedule by ID',
                'PUT /api/schedules/:id': 'Update schedule (admin only)',
                'PATCH /api/schedules/:id/status': 'Update schedule status (admin only)',
                'DELETE /api/schedules/:id': 'Delete schedule (admin only)'
            },
            admin: {
                'GET /api/admin/dashboard/stats': 'Get dashboard statistics (admin only)',
                'GET /api/admin/dashboard/activities': 'Get recent activities (admin only)',
                'GET /api/admin/dashboard/overview': 'Get system overview (admin only)',
                'POST /api/admin/reports/generate': 'Generate reports (admin only)',
                'GET /api/admin/profile': 'Get admin profile (admin only)',
                'PUT /api/admin/profile': 'Update admin profile (admin only)'
            }
        },
        authentication: 'Use Bearer token in Authorization header for protected routes'
    });
});

// Error handling middleware
app.use((err, req, res, next) => {
    console.error('Error Stack:', err.stack);
    console.error('Error Details:', err);

    // Database connection error
    if (err.code === 'ECONNREFUSED') {
        return res.status(503).json({
            success: false,
            message: 'Database connection failed. Please try again later.'
        });
    }

    // MySQL duplicate entry error
    if (err.code === 'ER_DUP_ENTRY') {
        return res.status(400).json({
            success: false,
            message: 'Duplicate entry. The resource already exists.'
        });
    }

    // JWT errors
    if (err.name === 'JsonWebTokenError') {
        return res.status(401).json({
            success: false,
            message: 'Invalid token'
        });
    }

    if (err.name === 'TokenExpiredError') {
        return res.status(401).json({
            success: false,
            message: 'Token expired'
        });
    }

    // Default error response
    res.status(err.status || 500).json({
        success: false,
        message: err.message || 'Internal server error',
        ...(process.env.NODE_ENV === 'development' && { stack: err.stack })
    });
});

// 404 handler for all unmatched routes - FIXED: Simple approach
app.use((req, res, next) => {
    if (req.path.startsWith('/api/')) {
        return res.status(404).json({
            success: false,
            message: `API endpoint ${req.method} ${req.originalUrl} not found`
        });
    }

    // For non-API routes
    res.status(404).json({
        success: false,
        message: 'Route not found. Please use /api endpoints.'
    });
});

// Root route handler
app.get('/', (req, res) => {
    res.json({
        success: true,
        message: 'Welcome to Dynamic Bus Schedule API',
        documentation: '/api',
        health: '/api/health'
    });
});

// Handle unhandled promise rejections
process.on('unhandledRejection', (err, promise) => {
    console.error('Unhandled Promise Rejection:', err);
    console.error('At promise:', promise);
});

// Handle uncaught exceptions
process.on('uncaughtException', (err) => {
    console.error('Uncaught Exception:', err);
    process.exit(1);
});

// Graceful shutdown
process.on('SIGTERM', () => {
    console.log('SIGTERM received, shutting down gracefully');
    if (server) {
        server.close(() => {
            console.log('Process terminated');
        });
    }
});

// Initialize database and start server
const startServer = async () => {
    try {
        console.log('Starting server initialization...');

        // Initialize database
        await db.initializeDatabase();
        console.log('✅ Database connected successfully');

        // Start server
        const server = app.listen(PORT, () => {
            console.log('\n' + '='.repeat(50));
            console.log('🚀 Dynamic Bus Schedule API Server Started');
            console.log('='.repeat(50));
            console.log(`📍 Server running on port: ${PORT}`);
            console.log(`🌐 Environment: ${process.env.NODE_ENV || 'development'}`);
            console.log(`🔗 Base URL: http://localhost:${PORT}`);
            console.log(`❤️  Health check: http://localhost:${PORT}/api/health`);
            console.log(`📚 API Docs: http://localhost:${PORT}/api`);
            console.log('='.repeat(50));
            console.log('\nAvailable Endpoints:');
            console.log('  POST   /api/auth/register');
            console.log('  POST   /api/auth/login');
            console.log('  GET    /api/auth/me');
            console.log('  GET    /api/users');
            console.log('  GET    /api/buses');
            console.log('  GET    /api/schedules');
            console.log('  GET    /api/admin/dashboard/stats');
            console.log('='.repeat(50));
        });

        return server;
    } catch (error) {
        console.error('❌ Failed to start server:', error);
        process.exit(1);
    }
};

// Start the server
startServer();