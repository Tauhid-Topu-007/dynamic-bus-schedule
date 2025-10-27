const User = require('../models/User');

const userController = {
    getAllUsers: async (req, res) => {
        try {
            const users = await User.findAll();
            res.json({
                success: true,
                data: users
            });
        } catch (error) {
            console.error('Get users error:', error);
            res.status(500).json({
                success: false,
                message: 'Server error'
            });
        }
    },

    getUserById: async (req, res) => {
        try {
            const user = await User.findById(req.params.id);
            if (!user) {
                return res.status(404).json({
                    success: false,
                    message: 'User not found'
                });
            }

            res.json({
                success: true,
                data: user
            });
        } catch (error) {
            console.error('Get user error:', error);
            res.status(500).json({
                success: false,
                message: 'Server error'
            });
        }
    },

    updateUser: async (req, res) => {
        try {
            const { name, email, phone, role } = req.body;
            const userId = req.params.id;

            const user = await User.findById(userId);
            if (!user) {
                return res.status(404).json({
                    success: false,
                    message: 'User not found'
                });
            }

            const updatedUser = await User.update(userId, { name, email, phone, role });

            res.json({
                success: true,
                message: 'User updated successfully',
                data: updatedUser
            });
        } catch (error) {
            console.error('Update user error:', error);
            res.status(500).json({
                success: false,
                message: 'Server error'
            });
        }
    },

    deleteUser: async (req, res) => {
        try {
            const userId = req.params.id;

            const user = await User.findById(userId);
            if (!user) {
                return res.status(404).json({
                    success: false,
                    message: 'User not found'
                });
            }

            await User.delete(userId);

            res.json({
                success: true,
                message: 'User deleted successfully'
            });
        } catch (error) {
            console.error('Delete user error:', error);
            res.status(500).json({
                success: false,
                message: 'Server error'
            });
        }
    },

    getDrivers: async (req, res) => {
        try {
            const drivers = await User.getDrivers();
            res.json({
                success: true,
                data: drivers
            });
        } catch (error) {
            console.error('Get drivers error:', error);
            res.status(500).json({
                success: false,
                message: 'Server error'
            });
        }
    },

    getClients: async (req, res) => {
        try {
            const clients = await User.getClients();
            res.json({
                success: true,
                data: clients
            });
        } catch (error) {
            console.error('Get clients error:', error);
            res.status(500).json({
                success: false,
                message: 'Server error'
            });
        }
    }
};

module.exports = userController;