const express = require('express');
const router = express.Router();
const userController = require('../controllers/userController');
const { auth, authorize } = require('../middleware/auth');

router.get('/', auth, authorize('admin'), userController.getAllUsers);
router.get('/drivers', auth, userController.getDrivers);
router.get('/clients', auth, authorize('admin'), userController.getClients);
router.get('/:id', auth, userController.getUserById);
router.put('/:id', auth, authorize('admin'), userController.updateUser);
router.delete('/:id', auth, authorize('admin'), userController.deleteUser);

module.exports = router;