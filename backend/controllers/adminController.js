const User = require('../models/User');
const Bus = require('../models/Bus');
const Schedule = require('../models/Schedule');
const db = require('../config/database');

const adminController = {
    getDashboardStats: async (req, res) => {
        try {
            console.log('Fetching dashboard statistics...');

            // Get total counts with proper error handling
            const usersCount = await db.query('SELECT COUNT(*) as total FROM users');
            const busesCount = await db.query('SELECT COUNT(*) as total FROM buses');
            const schedulesCount = await db.query('SELECT COUNT(*) as total FROM schedules');
            const activeSchedulesCount = await db.query('SELECT COUNT(*) as total FROM schedules WHERE status = "scheduled"');

            // Get today's schedules
            const todaysTripsCount = await db.query(
                'SELECT COUNT(*) as total FROM schedules WHERE DATE(departure_time) = CURDATE()'
            );

            // Get role-based user counts
            const adminsCount = await db.query('SELECT COUNT(*) as total FROM users WHERE role = "admin"');
            const driversCount = await db.query('SELECT COUNT(*) as total FROM users WHERE role = "driver"');
            const clientsCount = await db.query('SELECT COUNT(*) as total FROM users WHERE role = "client"');

            // Get bus status counts
            const activeBusesCount = await db.query('SELECT COUNT(*) as total FROM buses WHERE status = "active"');
            const maintenanceBusesCount = await db.query('SELECT COUNT(*) as total FROM buses WHERE status = "maintenance"');

            // Ensure we have valid data
            const totals = {
                users: usersCount[0]?.total || 0,
                buses: busesCount[0]?.total || 0,
                schedules: schedulesCount[0]?.total || 0,
                activeSchedules: activeSchedulesCount[0]?.total || 0,
                todaysTrips: todaysTripsCount[0]?.total || 0
            };

            const breakdown = {
                users: {
                    admins: adminsCount[0]?.total || 0,
                    drivers: driversCount[0]?.total || 0,
                    clients: clientsCount[0]?.total || 0
                },
                buses: {
                    active: activeBusesCount[0]?.total || 0,
                    maintenance: maintenanceBusesCount[0]?.total || 0
                }
            };

            console.log('Dashboard stats fetched successfully:', totals);

            res.json({
                success: true,
                data: {
                    totals,
                    breakdown,
                    recentActivities: [] // Empty for now, will be populated separately
                }
            });
        } catch (error) {
            console.error('Get dashboard stats error:', error);
            res.status(500).json({
                success: false,
                message: 'Server error while fetching dashboard statistics: ' + error.message
            });
        }
    },

    getRecentActivities: async (req, res) => {
        try {
            console.log('Fetching recent activities...');
            const { limit = 20 } = req.query;

            // Get recent users
            const recentUsers = await db.query(`
                SELECT
                    'user_registered' as type,
                    name as description,
                    created_at as timestamp,
                    CONCAT('User ', name, ' registered') as activity,
                    name as user,
                    email as user_email
                FROM users
                ORDER BY created_at DESC
                LIMIT 5
            `);

            // Get recent buses
            const recentBuses = await db.query(`
                SELECT
                    'bus_added' as type,
                    CONCAT('Bus ', bus_number, ' added') as description,
                    created_at as timestamp,
                    CONCAT('Bus ', bus_number, ' added to fleet') as activity,
                    'Admin' as user,
                    '' as user_email
                FROM buses
                ORDER BY created_at DESC
                LIMIT 5
            `);

            // Get recent schedules
            const recentSchedules = await db.query(`
                SELECT
                    'schedule_created' as type,
                    CONCAT('Schedule for ', route_name) as description,
                    created_at as timestamp,
                    CONCAT('New schedule created for ', route_name) as activity,
                    'Admin' as user,
                    '' as user_email
                FROM schedules
                ORDER BY created_at DESC
                LIMIT 5
            `);

            // Combine all activities and sort by timestamp
            let allActivities = [
                ...recentUsers,
                ...recentBuses,
                ...recentSchedules
            ];

            // Sort by timestamp (newest first) and limit
            allActivities.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
            allActivities = allActivities.slice(0, parseInt(limit));

            console.log(`Returning ${allActivities.length} recent activities`);

            res.json({
                success: true,
                data: allActivities
            });
        } catch (error) {
            console.error('Get recent activities error:', error);
            res.status(500).json({
                success: false,
                message: 'Server error while fetching recent activities: ' + error.message
            });
        }
    },

    getSystemOverview: async (req, res) => {
        try {
            console.log('Fetching system overview...');

            // Get weekly schedule data
            const weeklySchedules = await db.query(`
                SELECT
                    DAYNAME(departure_time) as day,
                    COUNT(*) as count
                FROM schedules
                WHERE departure_time >= DATE_SUB(NOW(), INTERVAL 7 DAY)
                GROUP BY DAYNAME(departure_time)
                ORDER BY FIELD(day, 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday')
            `);

            // Get popular routes
            const popularRoutes = await db.query(`
                SELECT
                    route_name,
                    departure_location,
                    arrival_location,
                    COUNT(*) as trip_count
                FROM schedules
                WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                GROUP BY route_name, departure_location, arrival_location
                ORDER BY trip_count DESC
                LIMIT 5
            `);

            // Get bus utilization
            const busUtilization = await db.query(`
                SELECT
                    b.bus_number,
                    COUNT(s.id) as schedule_count,
                    b.status
                FROM buses b
                LEFT JOIN schedules s ON b.id = s.bus_id
                WHERE s.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
                GROUP BY b.id, b.bus_number, b.status
                ORDER BY schedule_count DESC
                LIMIT 10
            `);

            res.json({
                success: true,
                data: {
                    weeklySchedules: weeklySchedules || [],
                    popularRoutes: popularRoutes || [],
                    busUtilization: busUtilization || []
                }
            });
        } catch (error) {
            console.error('Get system overview error:', error);
            res.status(500).json({
                success: false,
                message: 'Server error while fetching system overview: ' + error.message
            });
        }
    },

    generateReport: async (req, res) => {
        try {
            const { reportType, startDate, endDate } = req.body;

            if (!reportType) {
                return res.status(400).json({
                    success: false,
                    message: 'Report type is required'
                });
            }

            let reportData = {};
            let query = '';

            const start = startDate || '2024-01-01';
            const end = endDate || new Date().toISOString().split('T')[0];

            switch (reportType) {
                case 'user_registration':
                    query = `
                        SELECT
                            DATE(created_at) as date,
                            COUNT(*) as registrations,
                            role
                        FROM users
                        WHERE created_at BETWEEN ? AND ?
                        GROUP BY DATE(created_at), role
                        ORDER BY date
                    `;
                    break;

                case 'bus_utilization':
                    query = `
                        SELECT
                            b.bus_number,
                            COUNT(s.id) as trips_completed,
                            COALESCE(SUM(s.price), 0) as total_revenue,
                            COALESCE(AVG(s.available_seats), 0) as avg_occupancy
                        FROM buses b
                        LEFT JOIN schedules s ON b.id = s.bus_id
                        WHERE (s.departure_time BETWEEN ? AND ?) OR s.departure_time IS NULL
                        GROUP BY b.id, b.bus_number
                    `;
                    break;

                case 'schedule_performance':
                    query = `
                        SELECT
                            route_name,
                            departure_location,
                            arrival_location,
                            COUNT(*) as total_trips,
                            COALESCE(AVG(available_seats), 0) as avg_occupancy,
                            COALESCE(SUM(price), 0) as total_revenue
                        FROM schedules
                        WHERE departure_time BETWEEN ? AND ?
                        GROUP BY route_name, departure_location, arrival_location
                        ORDER BY total_revenue DESC
                    `;
                    break;

                default:
                    return res.status(400).json({
                        success: false,
                        message: 'Invalid report type. Use: user_registration, bus_utilization, or schedule_performance'
                    });
            }

            const results = await db.query(query, [start, end]);
            reportData = results;

            res.json({
                success: true,
                data: {
                    reportType,
                    startDate: start,
                    endDate: end,
                    generatedAt: new Date().toISOString(),
                    data: reportData
                }
            });
        } catch (error) {
            console.error('Generate report error:', error);
            res.status(500).json({
                success: false,
                message: 'Server error while generating report: ' + error.message
            });
        }
    },

    getAdminProfile: async (req, res) => {
        try {
            const adminUser = await User.findById(req.user.id);

            if (!adminUser) {
                return res.status(404).json({
                    success: false,
                    message: 'Admin user not found'
                });
            }

            res.json({
                success: true,
                data: adminUser
            });
        } catch (error) {
            console.error('Get admin profile error:', error);
            res.status(500).json({
                success: false,
                message: 'Server error while fetching admin profile: ' + error.message
            });
        }
    },

    updateAdminProfile: async (req, res) => {
        try {
            const { name, email, phone } = req.body;
            const adminId = req.user.id;

            // Validate input
            if (!name || !email) {
                return res.status(400).json({
                    success: false,
                    message: 'Name and email are required'
                });
            }

            const updatedAdmin = await User.update(adminId, {
                name,
                email,
                phone,
                role: 'admin' // Ensure role remains admin
            });

            res.json({
                success: true,
                message: 'Profile updated successfully',
                data: updatedAdmin
            });
        } catch (error) {
            console.error('Update admin profile error:', error);
            res.status(500).json({
                success: false,
                message: 'Server error while updating admin profile: ' + error.message
            });
        }
    }
};

module.exports = adminController;