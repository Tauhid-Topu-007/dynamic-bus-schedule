const express = require('express');
const router = express.Router();
const scheduleController = require('../controllers/scheduleController');
const { auth, authorize } = require('../middleware/auth');
const { scheduleValidation, handleValidationErrors } = require('../middleware/validation');

router.post('/', auth, authorize('admin'), scheduleValidation, handleValidationErrors, scheduleController.createSchedule);
router.get('/', auth, scheduleController.getAllSchedules);
router.get('/:id', auth, scheduleController.getScheduleById);
router.put('/:id', auth, authorize('admin'), scheduleValidation, handleValidationErrors, scheduleController.updateSchedule);
router.patch('/:id/status', auth, authorize('admin'), scheduleController.updateScheduleStatus);
router.delete('/:id', auth, authorize('admin'), scheduleController.deleteSchedule);

module.exports = router;