const Schedule = require('../models/Schedule');

const scheduleController = {
    createSchedule: async (req, res) => {
        try {
            const schedule = await Schedule.create(req.body);

            res.status(201).json({
                success: true,
                message: 'Schedule created successfully',
                data: schedule
            });
        } catch (error) {
            console.error('Create schedule error:', error);
            res.status(500).json({
                success: false,
                message: 'Server error'
            });
        }
    },

    getAllSchedules: async (req, res) => {
        try {
            const { departure_location, arrival_location, departure_date, status } = req.query;
            const filters = {};

            if (departure_location) filters.departure_location = departure_location;
            if (arrival_location) filters.arrival_location = arrival_location;
            if (departure_date) filters.departure_date = departure_date;
            if (status) filters.status = status;

            const schedules = await Schedule.findAll(filters);
            res.json({
                success: true,
                data: schedules
            });
        } catch (error) {
            console.error('Get schedules error:', error);
            res.status(500).json({
                success: false,
                message: 'Server error'
            });
        }
    },

    getScheduleById: async (req, res) => {
        try {
            const schedule = await Schedule.findById(req.params.id);
            if (!schedule) {
                return res.status(404).json({
                    success: false,
                    message: 'Schedule not found'
                });
            }

            res.json({
                success: true,
                data: schedule
            });
        } catch (error) {
            console.error('Get schedule error:', error);
            res.status(500).json({
                success: false,
                message: 'Server error'
            });
        }
    },

    updateSchedule: async (req, res) => {
        try {
            const schedule = await Schedule.findById(req.params.id);
            if (!schedule) {
                return res.status(404).json({
                    success: false,
                    message: 'Schedule not found'
                });
            }

            const updatedSchedule = await Schedule.update(req.params.id, req.body);

            res.json({
                success: true,
                message: 'Schedule updated successfully',
                data: updatedSchedule
            });
        } catch (error) {
            console.error('Update schedule error:', error);
            res.status(500).json({
                success: false,
                message: 'Server error'
            });
        }
    },

    deleteSchedule: async (req, res) => {
        try {
            const schedule = await Schedule.findById(req.params.id);
            if (!schedule) {
                return res.status(404).json({
                    success: false,
                    message: 'Schedule not found'
                });
            }

            await Schedule.delete(req.params.id);

            res.json({
                success: true,
                message: 'Schedule deleted successfully'
            });
        } catch (error) {
            console.error('Delete schedule error:', error);
            res.status(500).json({
                success: false,
                message: 'Server error'
            });
        }
    },

    updateScheduleStatus: async (req, res) => {
        try {
            const { status } = req.body;
            const schedule = await Schedule.findById(req.params.id);
            if (!schedule) {
                return res.status(404).json({
                    success: false,
                    message: 'Schedule not found'
                });
            }

            const updatedSchedule = await Schedule.updateStatus(req.params.id, status);

            res.json({
                success: true,
                message: 'Schedule status updated successfully',
                data: updatedSchedule
            });
        } catch (error) {
            console.error('Update schedule status error:', error);
            res.status(500).json({
                success: false,
                message: 'Server error'
            });
        }
    }
};

module.exports = scheduleController;