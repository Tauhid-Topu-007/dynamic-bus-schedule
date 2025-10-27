const Bus = require('../models/Bus');

const busController = {
    createBus: async (req, res) => {
        try {
            const bus = await Bus.create(req.body);

            res.status(201).json({
                success: true,
                message: 'Bus created successfully',
                data: bus
            });
        } catch (error) {
            console.error('Create bus error:', error);
            if (error.code === 'ER_DUP_ENTRY') {
                return res.status(400).json({
                    success: false,
                    message: 'Bus number or license plate already exists'
                });
            }
            res.status(500).json({
                success: false,
                message: 'Server error'
            });
        }
    },

    getAllBuses: async (req, res) => {
        try {
            const buses = await Bus.findAll();
            res.json({
                success: true,
                data: buses
            });
        } catch (error) {
            console.error('Get buses error:', error);
            res.status(500).json({
                success: false,
                message: 'Server error'
            });
        }
    },

    getBusById: async (req, res) => {
        try {
            const bus = await Bus.findById(req.params.id);
            if (!bus) {
                return res.status(404).json({
                    success: false,
                    message: 'Bus not found'
                });
            }

            res.json({
                success: true,
                data: bus
            });
        } catch (error) {
            console.error('Get bus error:', error);
            res.status(500).json({
                success: false,
                message: 'Server error'
            });
        }
    },

    updateBus: async (req, res) => {
        try {
            const bus = await Bus.findById(req.params.id);
            if (!bus) {
                return res.status(404).json({
                    success: false,
                    message: 'Bus not found'
                });
            }

            const updatedBus = await Bus.update(req.params.id, req.body);

            res.json({
                success: true,
                message: 'Bus updated successfully',
                data: updatedBus
            });
        } catch (error) {
            console.error('Update bus error:', error);
            res.status(500).json({
                success: false,
                message: 'Server error'
            });
        }
    },

    deleteBus: async (req, res) => {
        try {
            const bus = await Bus.findById(req.params.id);
            if (!bus) {
                return res.status(404).json({
                    success: false,
                    message: 'Bus not found'
                });
            }

            await Bus.delete(req.params.id);

            res.json({
                success: true,
                message: 'Bus deleted successfully'
            });
        } catch (error) {
            console.error('Delete bus error:', error);
            res.status(500).json({
                success: false,
                message: 'Server error'
            });
        }
    },

    updateBusStatus: async (req, res) => {
        try {
            const { status } = req.body;
            const bus = await Bus.findById(req.params.id);
            if (!bus) {
                return res.status(404).json({
                    success: false,
                    message: 'Bus not found'
                });
            }

            const updatedBus = await Bus.updateStatus(req.params.id, status);

            res.json({
                success: true,
                message: 'Bus status updated successfully',
                data: updatedBus
            });
        } catch (error) {
            console.error('Update bus status error:', error);
            res.status(500).json({
                success: false,
                message: 'Server error'
            });
        }
    }
};

module.exports = busController;