// payment-controller.js - Controlador de MercadoPago Bricks para FlowFit

/**
 * Controlador que maneja la integración con MercadoPago Checkout Bricks
 * Renderiza el formulario de pago y gestiona el flujo completo
 */
class PaymentController {
    constructor() {
        this.mp = null;
        this.bricksBuilder = null;
        this.cardPaymentBrickController = null;
        this.config = null;
        this.paymentService = new PaymentService();
    }

    /**
     * Inicializa MercadoPago Bricks con la configuración del backend
     * @param {Object} config - Configuración del pago {amount, email, description, negociacionId}
     */
    async initialize(config) {
        this.config = config;
        
        try {
            console.log('🚀 Inicializando MercadoPago Bricks...');
            console.log('📋 Configuración:', {
                amount: config.amount,
                email: config.email,
                description: config.description,
                negociacionId: config.negociacionId
            });
            
            // Obtener la public key desde el backend
            const mpConfig = await this.paymentService.initialize();
            
            console.log('🔑 Configuración obtenida:', {
                publicKey: mpConfig.publicKey?.substring(0, 20) + '...',
                locale: mpConfig.locale,
                testMode: mpConfig.testMode
            });
            
            // Inicializar MercadoPago
            this.mp = new MercadoPago(mpConfig.publicKey, {
                locale: mpConfig.locale || 'es-CO'
            });
            
            this.bricksBuilder = this.mp.bricks();
            
            console.log('✅ MercadoPago inicializado correctamente');
            
            // Renderizar el brick de pago
            await this.renderCardPaymentBrick();
            
        } catch (error) {
            console.error('❌ Error al inicializar MercadoPago:', error);
            this.showError('Error al cargar el sistema de pagos. Por favor, recarga la página.');
        }
    }

    /**
     * Renderiza el Card Payment Brick en el contenedor especificado
     */
    async renderCardPaymentBrick() {
        const settings = {
            initialization: {
                amount: this.config.amount
            },
            customization: {
                visual: {
                    style: {
                        theme: 'default'
                    }
                },
                paymentMethods: {
                    maxInstallments: 1
                }
            },
            callbacks: {
                onReady: () => {
                    console.log('✅ Brick de pago listo');
                    this.hideLoading();
                },
                onSubmit: (cardFormData) => {
                    return this.handlePaymentSubmit(cardFormData);
                },
                onError: (error) => {
                    console.error('❌ Error en el brick:', error);
                    this.showError('Ocurrió un error al procesar el formulario');
                }
            }
        };

        try {
            this.showLoading();
            
            console.log('🎨 Renderizando Card Payment Brick con settings:', settings);
            
            // Verificar que el contenedor existe
            const container = document.getElementById('cardPaymentBrick_container');
            if (!container) {
                throw new Error('Contenedor cardPaymentBrick_container no encontrado');
            }
            
            // Crear el brick en el contenedor
            this.cardPaymentBrickController = await this.bricksBuilder.create(
                'cardPayment', 
                'cardPaymentBrick_container', 
                settings
            );
            
            console.log('✅ Card Payment Brick renderizado correctamente');
            
        } catch (error) {
            console.error('❌ Error al crear el brick:', error);
            console.error('Detalles del error:', {
                message: error.message,
                cause: error.cause,
                stack: error.stack
            });
            this.showError('Error al cargar el formulario de pago: ' + error.message);
            this.hideLoading();
        }
    }

    /**
     * Maneja el envío del formulario cuando el usuario hace submit
     * @param {Object} cardFormData - Datos del formulario incluido el token
     */
    async handlePaymentSubmit(cardFormData) {
        this.showLoading();
        this.hideMessages();

        try {
            console.log('💳 Procesando pago con token generado por Bricks...');

            // Preparar los datos para el backend de FlowFit
            const paymentData = {
                transactionAmount: this.config.amount,
                token: cardFormData.token,
                description: this.config.description || 'Plan de entrenamiento FlowFit',
                installments: cardFormData.installments || 1,
                paymentMethodId: cardFormData.payment_method_id,
                issuer: cardFormData.issuer_id,
                negociacionId: this.config.negociacionId, // ID de la negociación en FlowFit
                payer: {
                    email: cardFormData.payer.email,
                    identification: {
                        type: cardFormData.payer.identification.type,
                        number: cardFormData.payer.identification.number
                    }
                }
            };

            // Enviar al backend de FlowFit
            const result = await this.paymentService.processPayment(paymentData);

            // Manejar respuesta exitosa
            this.handlePaymentSuccess(result);

        } catch (error) {
            console.error('❌ Error en el pago:', error);
            this.handlePaymentError(error);
            throw error; // Re-lanzar para que el brick maneje el error
        } finally {
            this.hideLoading();
        }
    }

    /**
     * Maneja el éxito del pago
     * @param {Object} result - Respuesta del backend con los detalles del pago
     */
    handlePaymentSuccess(result) {
        console.log('✅ Pago aprobado exitosamente');
        
        const successMsg = `
            <div class="text-center">
                <i class="fas fa-check-circle fa-3x text-success mb-3"></i>
                <h5>¡Pago exitoso!</h5>
                <p><strong>ID:</strong> ${result.id}</p>
                <p><strong>Estado:</strong> ${this.getStatusText(result.status)}</p>
                <p><strong>Monto:</strong> $${result.transactionAmount?.toLocaleString('es-CO') || this.config.amount.toLocaleString('es-CO')} COP</p>
            </div>
        `;
        
        this.showSuccess(successMsg);
        
        // Ocultar el formulario de pago
        const brickContainer = document.getElementById('cardPaymentBrick_container');
        if (brickContainer) {
            brickContainer.style.display = 'none';
        }

        // Recargar la página después de 2 segundos para reflejar cambios
        setTimeout(() => {
            console.log('🔄 Recargando página para actualizar estado...');
            location.reload();
        }, 2000);
    }

    /**
     * Maneja los errores durante el proceso de pago
     * @param {Error} error - Error capturado
     */
    handlePaymentError(error) {
        console.error('❌ Error en el proceso de pago');
        
        let errorMessage = 'Hubo un problema al procesar tu pago. ';
        
        if (error.message) {
            errorMessage += error.message;
        } else {
            errorMessage += 'Por favor, verifica los datos e intenta nuevamente.';
        }
        
        this.showError(errorMessage);
    }

    /**
     * Convierte el código de estado de MercadoPago a texto legible
     * @param {string} status - Código de estado
     * @returns {string} Texto descriptivo del estado
     */
    getStatusText(status) {
        const statusMap = {
            'approved': '✅ Aprobado',
            'pending': '⏳ Pendiente',
            'in_process': '🔄 En proceso',
            'rejected': '❌ Rechazado',
            'cancelled': '🚫 Cancelado',
            'refunded': '💰 Reembolsado',
            'charged_back': '↩️ Contracargo'
        };
        return statusMap[status] || status;
    }

    /**
     * Muestra el indicador de carga
     */
    showLoading() {
        const loadingElement = document.getElementById('loadingPayment');
        if (loadingElement) {
            loadingElement.style.display = 'block';
        }
    }

    /**
     * Oculta el indicador de carga
     */
    hideLoading() {
        const loadingElement = document.getElementById('loadingPayment');
        if (loadingElement) {
            loadingElement.style.display = 'none';
        }
    }

    /**
     * Muestra un mensaje de éxito
     * @param {string} message - Mensaje HTML a mostrar
     */
    showSuccess(message) {
        const container = document.getElementById('paymentMessages');
        if (container) {
            container.innerHTML = `
                <div class="alert alert-success alert-dismissible fade show" role="alert">
                    ${message}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            `;
            container.style.display = 'block';
        }
    }

    /**
     * Muestra un mensaje de error
     * @param {string} message - Mensaje de error
     */
    showError(message) {
        const container = document.getElementById('paymentMessages');
        if (container) {
            container.innerHTML = `
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    <i class="fas fa-exclamation-triangle me-2"></i>
                    ${message}
                    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                </div>
            `;
            container.style.display = 'block';
        }
    }

    /**
     * Oculta todos los mensajes
     */
    hideMessages() {
        const container = document.getElementById('paymentMessages');
        if (container) {
            container.innerHTML = '';
            container.style.display = 'none';
        }
    }

    /**
     * Destruye el brick y libera recursos
     */
    destroy() {
        if (this.cardPaymentBrickController) {
            this.cardPaymentBrickController.unmount();
            this.cardPaymentBrickController = null;
        }
        this.mp = null;
        this.bricksBuilder = null;
        console.log('🗑️ PaymentController destruido');
    }
}
