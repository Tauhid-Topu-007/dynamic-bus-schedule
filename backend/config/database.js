const mysql = require('mysql2/promise');
require('dotenv').config();

const dbConfig = {
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME
};

class Database {
    constructor() {
        this.pool = mysql.createPool({
            ...dbConfig,
            waitForConnections: true,
            connectionLimit: 10,
            queueLimit: 0
        });
    }

    async query(sql, params = []) {
        try {
            const [rows] = await this.pool.execute(sql, params);
            return rows;
        } catch (error) {
            console.error('Database query error:', error);
            throw error;
        }
    }

    async getConnection() {
        return await this.pool.getConnection();
    }

    async initializeDatabase() {
        try {
            console.log('🔄 Initializing database...');

            // Create database if it doesn't exist
            const connection = await mysql.createConnection({
                host: process.env.DB_HOST,
                user: process.env.DB_USER,
                password: process.env.DB_PASSWORD
            });

            await connection.execute(`CREATE DATABASE IF NOT EXISTS ${process.env.DB_NAME}`);
            await connection.end();

            // Create tables
            await this.createTables();
            console.log('✅ Database initialized successfully');
        } catch (error) {
            console.error('❌ Database initialization error:', error);
            throw error;
        }
    }

    async createTables() {
        try {
            console.log('🔄 Creating database tables...');

            // Users table
            await this.query(`
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    email VARCHAR(255) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    phone VARCHAR(20),
                    role ENUM('admin', 'client', 'driver') NOT NULL DEFAULT 'client',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
            `);
            console.log('✅ Users table created/verified');

            // Buses table
            await this.query(`
                CREATE TABLE IF NOT EXISTS buses (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    bus_number VARCHAR(50) UNIQUE NOT NULL,
                    license_plate VARCHAR(20) UNIQUE NOT NULL,
                    capacity INT NOT NULL,
                    model VARCHAR(100),
                    status ENUM('active', 'maintenance', 'inactive') DEFAULT 'active',
                    driver_id INT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    FOREIGN KEY (driver_id) REFERENCES users(id) ON DELETE SET NULL
                )
            `);
            console.log('✅ Buses table created/verified');

            // Schedules table
            await this.query(`
                CREATE TABLE IF NOT EXISTS schedules (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    bus_id INT NOT NULL,
                    route_name VARCHAR(255) NOT NULL,
                    departure_location VARCHAR(255) NOT NULL,
                    arrival_location VARCHAR(255) NOT NULL,
                    departure_time DATETIME NOT NULL,
                    arrival_time DATETIME NOT NULL,
                    price DECIMAL(10,2) NOT NULL,
                    available_seats INT NOT NULL,
                    status ENUM('scheduled', 'departed', 'arrived', 'cancelled') DEFAULT 'scheduled',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    FOREIGN KEY (bus_id) REFERENCES buses(id) ON DELETE CASCADE
                )
            `);
            console.log('✅ Schedules table created/verified');

            // Create default admin user
            await this.createDefaultAdmin();

            // Create sample data for testing
            await this.createSampleData();

        } catch (error) {
            console.error('❌ Error creating tables:', error);
            throw error;
        }
    }

    async createDefaultAdmin() {
        try {
            const bcrypt = require('bcryptjs');
            const hashedPassword = await bcrypt.hash('admin123', 10);

            const existingAdmin = await this.query('SELECT * FROM users WHERE email = ?', ['admin@bus.com']);
            if (existingAdmin.length === 0) {
                await this.query(
                    'INSERT INTO users (name, email, password, phone, role) VALUES (?, ?, ?, ?, ?)',
                    ['System Admin', 'admin@bus.com', hashedPassword, '+8801711111111', 'admin']
                );
                console.log('✅ Default admin user created: admin@bus.com / admin123');
            } else {
                console.log('✅ Default admin user already exists');
            }
        } catch (error) {
            console.error('❌ Error creating default admin:', error);
        }
    }

    async createSampleData() {
        try {
            console.log('🔄 Creating sample data...');

            // Check if we already have sample data
            const userCount = await this.query('SELECT COUNT(*) as count FROM users');
            const busCount = await this.query('SELECT COUNT(*) as count FROM buses');
            const scheduleCount = await this.query('SELECT COUNT(*) as count FROM schedules');

            const totalUsers = userCount[0].count;
            const totalBuses = busCount[0].count;
            const totalSchedules = scheduleCount[0].count;

            console.log(`📊 Current data: ${totalUsers} users, ${totalBuses} buses, ${totalSchedules} schedules`);

            // Create sample drivers and clients if we have less than 5 users (excluding admin)
            if (totalUsers <= 1) {
                await this.createSampleUsers();
            }

            // Create sample buses if we have less than 3 buses
            if (totalBuses < 3) {
                await this.createSampleBuses();
            }

            // Create sample schedules if we have less than 5 schedules
            if (totalSchedules < 5) {
                await this.createSampleSchedules();
            }

            console.log('✅ Sample data creation completed');

        } catch (error) {
            console.log('⚠️ Sample data creation warning:', error.message);
        }
    }

    async createSampleUsers() {
        try {
            const bcrypt = require('bcryptjs');
            const hashedPassword = await bcrypt.hash('password123', 10);

            const sampleUsers = [
                ['John Driver', 'driver.john@bus.com', hashedPassword, '+8801712345678', 'driver'],
                ['Sarah Client', 'sarah.client@bus.com', hashedPassword, '+8801812345678', 'client'],
                ['Mike Operator', 'mike.driver@bus.com', hashedPassword, '+8801912345678', 'driver'],
                ['Emma Traveler', 'emma.client@bus.com', hashedPassword, '+8801612345678', 'client'],
                ['David Manager', 'david.admin@bus.com', hashedPassword, '+8801512345678', 'admin'],
                ['Lisa Passenger', 'lisa.client@bus.com', hashedPassword, '+8801412345678', 'client']
            ];

            for (const user of sampleUsers) {
                try {
                    await this.query(
                        'INSERT IGNORE INTO users (name, email, password, phone, role) VALUES (?, ?, ?, ?, ?)',
                        user
                    );
                } catch (error) {
                    // Ignore duplicate entries
                    if (!error.message.includes('Duplicate')) {
                        console.log('⚠️ Error inserting user:', error.message);
                    }
                }
            }
            console.log('✅ Sample users created');
        } catch (error) {
            console.log('⚠️ Error creating sample users:', error.message);
        }
    }

    async createSampleBuses() {
        try {
            // Get driver IDs
            const drivers = await this.query('SELECT id FROM users WHERE role = "driver"');

            const sampleBuses = [
                ['BUS-001', 'DHA-1234', 40, 'Volvo B9R', 'active', drivers[0]?.id || null],
                ['BUS-002', 'DHA-5678', 35, 'Scania K-series', 'active', drivers[1]?.id || null],
                ['BUS-003', 'DHA-9012', 45, 'Mercedes-Benz', 'maintenance', null],
                ['BUS-004', 'DHA-3456', 30, 'Ashok Leyland', 'active', null],
                ['BUS-005', 'DHA-7890', 50, 'Toyota Coaster', 'active', null],
                ['BUS-006', 'DHA-1111', 40, 'Hino Bus', 'inactive', null],
                ['BUS-007', 'DHA-2222', 38, 'Tata Starbus', 'active', null]
            ];

            for (const bus of sampleBuses) {
                try {
                    await this.query(
                        'INSERT IGNORE INTO buses (bus_number, license_plate, capacity, model, status, driver_id) VALUES (?, ?, ?, ?, ?, ?)',
                        bus
                    );
                } catch (error) {
                    // Ignore duplicate entries
                    if (!error.message.includes('Duplicate')) {
                        console.log('⚠️ Error inserting bus:', error.message);
                    }
                }
            }
            console.log('✅ Sample buses created');
        } catch (error) {
            console.log('⚠️ Error creating sample buses:', error.message);
        }
    }

    async createSampleSchedules() {
        try {
            // Get bus IDs
            const buses = await this.query('SELECT id FROM buses');
            if (buses.length === 0) {
                console.log('⚠️ No buses available for creating schedules');
                return;
            }

            // Create schedules for the next 14 days
            const today = new Date();
            const routes = [
                { name: 'Dhaka to Chittagong', departure: 'Dhaka', arrival: 'Chittagong', price: 800, duration: 6 },
                { name: 'Dhaka to Sylhet', departure: 'Dhaka', arrival: 'Sylhet', price: 700, duration: 5 },
                { name: 'Dhaka to Khulna', departure: 'Dhaka', arrival: 'Khulna', price: 600, duration: 4 },
                { name: 'Chittagong to Cox\'s Bazar', departure: 'Chittagong', arrival: 'Cox\'s Bazar', price: 400, duration: 3 },
                { name: 'Dhaka to Rajshahi', departure: 'Dhaka', arrival: 'Rajshahi', price: 550, duration: 4 },
                { name: 'Sylhet to Dhaka', departure: 'Sylhet', arrival: 'Dhaka', price: 700, duration: 5 },
                { name: 'Chittagong to Dhaka', departure: 'Chittagong', arrival: 'Dhaka', price: 800, duration: 6 }
            ];

            let schedulesCreated = 0;

            for (let day = 0; day < 14; day++) {
                for (let hour = 6; hour <= 20; hour += 2) { // From 6 AM to 8 PM every 2 hours
                    const routeIndex = (day + hour) % routes.length;
                    const busIndex = (day + hour) % buses.length;
                    const route = routes[routeIndex];

                    const departureTime = new Date(today);
                    departureTime.setDate(today.getDate() + day);
                    departureTime.setHours(hour, 0, 0, 0);

                    const arrivalTime = new Date(departureTime);
                    arrivalTime.setHours(departureTime.getHours() + route.duration);

                    // Random available seats (60-95% capacity)
                    const busCapacity = 40; // Default capacity
                    const availableSeats = Math.floor(busCapacity * (0.6 + Math.random() * 0.35));

                    try {
                        await this.query(`
                            INSERT IGNORE INTO schedules
                            (bus_id, route_name, departure_location, arrival_location, departure_time, arrival_time, price, available_seats)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        `, [
                            buses[busIndex].id,
                            route.name,
                            route.departure,
                            route.arrival,
                            departureTime,
                            arrivalTime,
                            route.price,
                            availableSeats
                        ]);
                        schedulesCreated++;
                    } catch (error) {
                        // Ignore duplicate entries
                        if (!error.message.includes('Duplicate')) {
                            console.log('⚠️ Error inserting schedule:', error.message);
                        }
                    }
                }
            }

            console.log(`✅ ${schedulesCreated} sample schedules created`);
        } catch (error) {
            console.log('⚠️ Error creating sample schedules:', error.message);
        }
    }

    // Helper method to reset database (for development)
    async resetDatabase() {
        try {
            console.log('🔄 Resetting database...');

            // Drop tables in correct order to handle foreign key constraints
            await this.query('SET FOREIGN_KEY_CHECKS = 0');
            await this.query('DROP TABLE IF EXISTS schedules');
            await this.query('DROP TABLE IF EXISTS buses');
            await this.query('DROP TABLE IF EXISTS users');
            await this.query('SET FOREIGN_KEY_CHECKS = 1');

            // Recreate tables
            await this.createTables();

            console.log('✅ Database reset successfully');
        } catch (error) {
            console.error('❌ Database reset error:', error);
            throw error;
        }
    }

    // Helper method to check database connection
    async checkConnection() {
        try {
            const connection = await this.getConnection();
            await connection.execute('SELECT 1');
            connection.release();
            console.log('✅ Database connection successful');
            return true;
        } catch (error) {
            console.error('❌ Database connection failed:', error.message);
            return false;
        }
    }

    // Method to get database statistics
    async getDatabaseStats() {
        try {
            const [userStats] = await this.query('SELECT COUNT(*) as total, role FROM users GROUP BY role');
            const [busStats] = await this.query('SELECT COUNT(*) as total, status FROM buses GROUP BY status');
            const [scheduleStats] = await this.query('SELECT COUNT(*) as total, status FROM schedules GROUP BY status');

            return {
                users: userStats,
                buses: busStats,
                schedules: scheduleStats
            };
        } catch (error) {
            console.error('Error getting database stats:', error);
            return null;
        }
    }
}

module.exports = new Database();