const express = require('express');
const router = express.Router();
const busController = require('../controllers/busController');
const { auth, authorize } = require('../middleware/auth');
const { busValidation, handleValidationErrors } = require('../middleware/validation');

router.post('/', auth, authorize('admin'), busValidation, handleValidationErrors, busController.createBus);
router.get('/', auth, busController.getAllBuses);
router.get('/:id', auth, busController.getBusById);
router.put('/:id', auth, authorize('admin'), busValidation, handleValidationErrors, busController.updateBus);
router.patch('/:id/status', auth, authorize('admin'), busController.updateBusStatus);
router.delete('/:id', auth, authorize('admin'), busController.deleteBus);

module.exports = router;