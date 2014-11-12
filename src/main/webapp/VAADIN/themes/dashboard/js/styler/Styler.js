/**
* Copyright (c) 2008-2011 The Open Planning Project
*
* Published under the GPL license.
* See https://github.com/opengeo/gxp/raw/master/license.txt for the full text
* of the license.
*/

/** api: (define)
 *  module = gxp.slider
 *  class = Tip
 *  base_link = `Ext.slider.Tip <http://extjs.com/deploy/dev/docs/?class=Ext.slider.Tip>`_
 */
Ext.namespace("gxp.slider");

/** api: constructor
 *  .. class:: gxp.slider.Tip(config)
 *
 *    See http://trac.geoext.org/ticket/262
 *
 *    This tip matches the Ext.slider.Tip but addes the hover functionality.
 */
gxp.slider.Tip = Ext.extend(Ext.slider.Tip, {

    /** api: config[hover]
     *  ``Boolean`` Display the tip when hovering over a thumb.  If false, tip
     *     will only be displayed while dragging.  Default is true.
     */
    hover: true,
    
    /** private: property[dragging]
     * ``Boolean`` A thumb is currently being dragged.
     */
    dragging: false,

    /** private: method[init]
     *  :arg slider: ``Object``
     */
    init: function(slider) {
        if(this.hover) {
            slider.on("render", this.registerThumbListeners, this);
        }
        this.slider = slider;
        gxp.slider.Tip.superclass.init.apply(this, arguments);
    },
    
    /** private: method[registerThumbListeners]
     */
    registerThumbListeners: function() {
        for(var i=0, len=this.slider.thumbs.length; i<len; ++i) {
            this.slider.thumbs[i].el.on({
                "mouseover": this.createHoverListener(i),
                "mouseout": function() {
                    if(!this.dragging) {
                        this.hide.apply(this, arguments);
                    }
                },
                scope: this
            });
        }
    },
    
    /** private: method[createHoverListener]
     */
    createHoverListener: function(index) {
        return (function() {
            this.onSlide(this.slider, {}, this.slider.thumbs[index]);
            this.dragging = false;
        }).createDelegate(this);
    },

    /** private: method[onSlide]
     */
    onSlide: function(slider, e, thumb) {
        this.dragging = true;
        gxp.slider.Tip.superclass.onSlide.apply(this, arguments);
    }

});
/**
* Copyright (c) 2008-2011 The Open Planning Project
*
* Published under the GPL license.
* See https://github.com/opengeo/gxp/raw/master/license.txt for the full text
* of the license.
*/

/**
 * @include widgets/tips/SliderTip.js
 */

/** api: (define)
 *  module = gxp
 *  class = ScaleLimitPanel
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
Ext.namespace("gxp");

/** api: constructor
 *  .. class:: ScaleLimitPanel(config)
 *   
 *      A panel for assembling scale constraints in SLD styles.
 */
gxp.ScaleLimitPanel = Ext.extend(Ext.Panel, {
    
    /** api: config[maxScaleDenominatorLimit]
     *  ``Number`` Upper limit for scale denominators.  Default is what you get
     *     when you project the world in Spherical Mercator onto a single
     *     256 x 256 pixel tile and assume OpenLayers.DOTS_PER_INCH (this
     *     corresponds to zoom level 0 in Google Maps).
     */
    maxScaleDenominatorLimit: 40075016.68 * 39.3701 * OpenLayers.DOTS_PER_INCH / 256,
    
    /** api: config[limitMaxScaleDenominator]
     *  ``Boolean`` Limit the maximum scale denominator.  If false, no upper
     *     limit will be imposed.
     */
    limitMaxScaleDenominator: true,

    /** api: config[maxScaleDenominator]
     *  ``Number`` The initial maximum scale denominator.  If <limitMaxScaleDenominator> is
     *     true and no minScaleDenominator is provided, <maxScaleDenominatorLimit> will
     *     be used.
     */
    maxScaleDenominator: undefined,

    /** api: config[minScaleDenominatorLimit]
     *  ``Number`` Lower limit for scale denominators.  Default is what you get when
     *     you assume 20 zoom levels starting with the world in Spherical
     *     Mercator on a single 256 x 256 tile at zoom 0 where the zoom factor
     *     is 2.
     */
    minScaleDenominatorLimit: Math.pow(0.5, 19) * 40075016.68 * 39.3701 * OpenLayers.DOTS_PER_INCH / 256,

    /** api: config[limitMinScaleDenominator]
     *  ``Boolean`` Limit the minimum scale denominator.  If false, no lower
     *     limit will be imposed.
     */
    limitMinScaleDenominator: true,

    /** api: config[minScaleDenominator]
     *  ``Number`` The initial minimum scale denominator.  If <limitMinScaleDenominator> is
     *     true and no minScaleDenominator is provided, <minScaleDenominatorLimit> will
     *     be used.
     */
    minScaleDenominator: undefined,
    
    /** api: config[scaleLevels]
     *  ``Number`` Number of scale levels to assume.  This is only for scaling
     *     values exponentially along the slider.  Scale values are not
     *     required to one of the discrete levels.  Default is 20.
     */
    scaleLevels: 20,
    
    /** api: config[scaleSliderTemplate]
     *  ``String`` Template for the tip displayed by the scale threshold slider.
     *
     *  Can be customized using the following keywords in curly braces:
     *
     *  * zoom - the zoom level
     *  * scale - the scale denominator
     *  * type - "Max" or "Min" denominator
     *  * scaleType - "Min" or "Max" scale (sense is opposite of type)
     *
     *  Default is "{scaleType} Scale 1:{scale}".
     */
    scaleSliderTemplate: "{scaleType} Scale 1:{scale}",
    
    /** api: config[modifyScaleTipContext]
     *  ``Function`` Called from the multi-slider tip's getText function.  The
     *     function will receive two arguments - a reference to the panel and
     *     a data object.  The data object will have scale, zoom, and type
     *     properties already calculated.  Other properties added to the data
     *     object  are available to the <scaleSliderTemplate>.
     */
    modifyScaleTipContext: Ext.emptyFn,

    /** private: property[scaleFactor]
     *  ``Number`` Calculated base for determining exponential scaling of values
     *     for the slider.
     */
    scaleFactor: null,
    
    /** private: property[changing]
     *  ``Boolean`` The panel is updating itself.
     */
    changing: false,
    
    border: false,
    
    /** i18n */
    maxScaleLimitText: "Max scale limit",
    minScaleLimitText: "Min scale limit",
    
    /** private: method[initComponent]
     */
    initComponent: function() {
        
        this.layout = "column";
        
        this.defaults = {
            border: false,
            bodyStyle: "margin: 0 5px;"
        };
        this.bodyStyle = {
            padding: "5px"
        };
        
        this.scaleSliderTemplate = new Ext.Template(this.scaleSliderTemplate);
        
        Ext.applyIf(this, {
            minScaleDenominator: this.minScaleDenominatorLimit,
            maxScaleDenominator: this.maxScaleDenominatorLimit
        });
        
        this.scaleFactor = Math.pow(
            this.maxScaleDenominatorLimit / this.minScaleDenominatorLimit,
            1 / (this.scaleLevels - 1)
        );
        
        this.scaleSlider = new Ext.Slider({
            vertical: true,
            height: 100,
            values: [0, 100],
            listeners: {
                changecomplete: function(slider, value) {
                    this.updateScaleValues(slider);
                },
                render: function(slider) {
                    slider.thumbs[0].el.setVisible(this.limitMaxScaleDenominator);
                    slider.thumbs[1].el.setVisible(this.limitMinScaleDenominator);
                    slider.setDisabled(!this.limitMinScaleDenominator && !this.limitMaxScaleDenominator);
                },
                scope: this
            },
            plugins: [new gxp.slider.Tip({
                getText: (function(thumb) {
                    var index = thumb.slider.thumbs.indexOf(thumb);
                    var value = thumb.value;
                    var scales = this.sliderValuesToScale([thumb.value]);
                    var data = {
                        scale: String(scales[0]),
                        zoom: (thumb.value * (this.scaleLevels / 100)).toFixed(1),
                        type: (index === 0) ? "Max" : "Min",
                        scaleType: (index === 0) ? "Min" : "Max"
                    };
                    this.modifyScaleTipContext(this, data);
                    return this.scaleSliderTemplate.apply(data);
                }).createDelegate(this)
            })]
        });
        
        this.maxScaleDenominatorInput = new Ext.form.NumberField({
            allowNegative: false,
            width: 100,
            fieldLabel: "1",
            value: Math.round(this.maxScaleDenominator),
            disabled: !this.limitMaxScaleDenominator,
            validator: (function(value) {
                return !this.limitMinScaleDenominator || (value > this.minScaleDenominator);
            }).createDelegate(this),
            listeners: {
                valid: function(field) {
                    var value = Number(field.getValue());
                    var limit = Math.round(this.maxScaleDenominatorLimit);
                    if(value < limit && value > this.minScaleDenominator) {
                        this.maxScaleDenominator = value;
                        this.updateSliderValues();
                    }
                },
                change: function(field) {
                    var value = Number(field.getValue());
                    var limit = Math.round(this.maxScaleDenominatorLimit);
                    if(value > limit) {
                        field.setValue(limit);
                    } else if(value < this.minScaleDenominator) {
                        field.setValue(this.minScaleDenominator);
                    } else {
                        this.maxScaleDenominator = value;
                        this.updateSliderValues();
                    }
                },
                scope: this
            }
        });

        this.minScaleDenominatorInput = new Ext.form.NumberField({
            allowNegative: false,
            width: 100,
            fieldLabel: "1",
            value: Math.round(this.minScaleDenominator),
            disabled: !this.limitMinScaleDenominator,
            validator: (function(value) {
                return !this.limitMaxScaleDenominator || (value < this.maxScaleDenominator);
            }).createDelegate(this),
            listeners: {
                valid: function(field) {
                    var value = Number(field.getValue());
                    var limit = Math.round(this.minScaleDenominatorLimit);
                    if(value > limit && value < this.maxScaleDenominator) {
                        this.minScaleDenominator = value;
                        this.updateSliderValues();
                    }
                },
                change: function(field) {
                    var value = Number(field.getValue());
                    var limit = Math.round(this.minScaleDenominatorLimit);
                    if(value < limit) {
                        field.setValue(limit);
                    } else if(value > this.maxScaleDenominator) {
                        field.setValue(this.maxScaleDenominator);
                    } else {
                        this.minScaleDenominator = value;
                        this.updateSliderValues();
                    }
                },
                scope: this
            }
        });
        
        this.items = [this.scaleSlider, {
            xtype: "panel",
            layout: "form",
            defaults: {border: false},
            items: [{
                labelWidth: 90,
                layout: "form",
                width: 150,
                items: [{
                    xtype: "checkbox",
                    checked: !!this.limitMinScaleDenominator,
                    fieldLabel: this.maxScaleLimitText,
                    listeners: {
                        check: function(box, checked) {
                            this.limitMinScaleDenominator = checked;
                            var slider = this.scaleSlider;
                            slider.setValue(1, 100);
                            slider.thumbs[1].el.setVisible(checked);
                            this.minScaleDenominatorInput.setDisabled(!checked);
                            this.updateScaleValues(slider);
                            slider.setDisabled(!this.limitMinScaleDenominator && !this.limitMaxScaleDenominator);
                        },
                        scope: this
                    }
                }]
            }, {
                labelWidth: 10,
                layout: "form",
                items: [this.minScaleDenominatorInput]
            }, {
                labelWidth: 90,
                layout: "form",
                items: [{
                    xtype: "checkbox",
                    checked: !!this.limitMaxScaleDenominator,
                    fieldLabel: this.minScaleLimitText,
                    listeners: {
                        check: function(box, checked) {
                            this.limitMaxScaleDenominator = checked;
                            var slider = this.scaleSlider;
                            slider.setValue(0, 0);
                            slider.thumbs[0].el.setVisible(checked);
                            this.maxScaleDenominatorInput.setDisabled(!checked);
                            this.updateScaleValues(slider);
                            slider.setDisabled(!this.limitMinScaleDenominator && !this.limitMaxScaleDenominator);
                        },
                        scope: this
                    }
                }]
            }, {
                labelWidth: 10,
                layout: "form",
                items: [this.maxScaleDenominatorInput]
            }]
        }];

        this.addEvents(
            /**
             * Event: change
             * Fires before any field blurs if the field value has changed.
             *
             * Listener arguments:
             * symbolizer - {Object} A symbolizer with fill related properties
             *     updated.
             */
            "change"
        ); 

        gxp.ScaleLimitPanel.superclass.initComponent.call(this);
        
    },
    
    /** private: method[updateScaleValues]
     */
    updateScaleValues: function(slider) {
        if(!this.changing) {
            var values = slider.getValues();
            var resetSlider = false;
            if(!this.limitMaxScaleDenominator) {
                if(values[0] > 0) {
                    values[0] = 0;
                    resetSlider = true;
                }
            }
            if(!this.limitMinScaleDenominator) {
                if(values[1] < 100) {
                    values[1] = 100;
                    resetSlider = true;
                }
            }
            if(resetSlider) {
                slider.setValue(0, values[0]);
                slider.setValue(1, values[1]);
            } else {
                var scales = this.sliderValuesToScale(values);
                var max = scales[0];
                var min = scales[1];
                this.changing = true;
                this.minScaleDenominatorInput.setValue(min);
                this.maxScaleDenominatorInput.setValue(max);
                this.changing = false;
                this.fireEvent(
                    "change", this,
                    (this.limitMinScaleDenominator) ? min : undefined,
                    (this.limitMaxScaleDenominator) ? max : undefined
                );
            }
        }
    },
    
    /** private: method[updateSliderValues]
     */
    updateSliderValues: function() {
        if(!this.changing) {
            var min = this.minScaleDenominator;
            var max = this.maxScaleDenominator;
            var values = this.scaleToSliderValues([max, min]);
            this.changing = true;
            this.scaleSlider.setValue(0, values[0]);
            this.scaleSlider.setValue(1, values[1]);
            this.changing = false;
            this.fireEvent(
                "change", this,
                (this.limitMinScaleDenominator) ? min : undefined,
                (this.limitMaxScaleDenominator) ? max : undefined
            );
        }
    },

    /** private: method[sliderValuesToScale]
     *  :arg values: ``Array`` Values from the scale slider.
     *  :return: ``Array`` A two item array of min and max scale denominators.
     *  
     *  Given two values between 0 and 100, generate the min and max scale
     *  denominators.  Assuming exponential scaling with <scaleFactor>.
     */
    sliderValuesToScale: function(values) {
        var interval = 100 / (this.scaleLevels - 1);
        return [Math.round(Math.pow(this.scaleFactor, (100 - values[0]) / interval) * this.minScaleDenominatorLimit),
                Math.round(Math.pow(this.scaleFactor, (100 - values[1]) / interval) * this.minScaleDenominatorLimit)];
    },
    
    /** private: method[scaleToSliderValues]
     */
    scaleToSliderValues: function(scales) {
        var interval = 100 / (this.scaleLevels - 1);
        return [100 - (interval * Math.log(scales[0] / this.minScaleDenominatorLimit) / Math.log(this.scaleFactor)),
                100 - (interval * Math.log(scales[1] / this.minScaleDenominatorLimit) / Math.log(this.scaleFactor))];
    }
    
});

/** api: xtype = gxp_scalelimitpanel */
Ext.reg('gxp_scalelimitpanel', gxp.ScaleLimitPanel); /**
* Copyright (c) 2008-2011 The Open Planning Project
*
* Published under the GPL license.
* See https://github.com/opengeo/gxp/raw/master/license.txt for the full text
* of the license.
*/

/** api: (define)
 *  module = gxp.form
 *  class = FontComboBox
 *  base_link = `Ext.form.ComboBox <http://extjs.com/deploy/dev/docs/?class=Ext.form.ComboBox>`_
 */
Ext.namespace("gxp.form");

/** api: constructor
 *  .. class:: FontComboBox(config)
 *   
 *      A combo box for selecting a font.
 */
gxp.form.FontComboBox = Ext.extend(Ext.form.ComboBox, {
    
    /** api: property[fonts]
     *  ``Array``
     *  List of font families to choose from.  Default is ["Arial",
     *  "Courier New", "Tahoma", "Times New Roman", "Verdana"].
     */
    fonts: [
        "Serif",
        "SansSerif",
        "Arial",
        "Courier New",
        "Tahoma",
        "Times New Roman",
        "Verdana"
    ],
    
    /** api: property[defaultFont]
     *  ``String``
     *  The ``fonts`` item to select by default.
     */
    defaultFont: "Serif",

    allowBlank: false,

    mode: "local",

    triggerAction: "all",

    editable: false,
  
    initComponent: function() {
        var fonts = this.fonts || gxp.form.FontComboBox.prototype.fonts;
        var defaultFont = this.defaultFont;
        if (fonts.indexOf(this.defaultFont) === -1) {
            defaultFont = fonts[0];
        }
        var defConfig = {
            displayField: "field1",
            valueField: "field1",
            store: fonts,
            value: defaultFont,
            tpl: new Ext.XTemplate(
                '<tpl for=".">' +
                    '<div class="x-combo-list-item">' +
                    '<span style="font-family: {field1};">{field1}</span>' +
                '</div></tpl>'
            )
        };
        Ext.applyIf(this, defConfig);
        
        gxp.form.FontComboBox.superclass.initComponent.call(this);
    }
});

/** api: xtype = gxp_fontcombo */
Ext.reg("gxp_fontcombo", gxp.form.FontComboBox);
/**
* Copyright (c) 2008-2011 The Open Planning Project
*
* Published under the GPL license.
* See https://github.com/opengeo/gxp/raw/master/license.txt for the full text
* of the license.
*/

/**
 * @include widgets/FillSymbolizer.js
 * @include widgets/form/FontComboBox.js
 */

/** api: (define)
 *  module = gxp
 *  class = TextSymbolizer
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
Ext.namespace("gxp");

/** api: constructor
 *  .. class:: TextSymbolizer(config)
 *   
 *      Form for configuring a text symbolizer.
 */
gxp.TextSymbolizer = Ext.extend(Ext.Panel, {
    
    /** api: config[fonts]
     *  ``Array(String)``
     *  List of fonts for the font combo.  If not set, defaults to the list
     *  provided by the :class:`gxp.FontComboBox`.
     */
    fonts: undefined,
    
    /** api: config[symbolizer]
     *  ``Object``
     *  A symbolizer object that will be used to fill in form values.
     *  This object will be modified when values change.  Clone first if
     *  you do not want your symbolizer modified.
     */
    symbolizer: null,
    
    /** api: config[defaultSymbolizer]
     *  ``Object``
     *  Default symbolizer properties to be used where none provided.
     */
    defaultSymbolizer: null,
    
    /** api: config[attributes]
     *  :class:`GeoExt.data.AttributeStore`
     *  A configured attributes store for use in the filter property combo.
     */
    attributes: null,
    
    /** api: config[colorManager]
     *  ``Function``
     *  Optional color manager constructor to be used as a plugin for the color
     *  field.
     */
    colorManager: null,

    /** private: property[haloCache]
     *  ``Object``
     *  Stores halo properties while fieldset is collapsed.
     */
    haloCache: null,
    
    border: false,    
    layout: "form",
    
    /** i18n */
    labelValuesText: "Label values",
    haloText: "Halo",
    sizeText: "Size",
    
    initComponent: function() {
        
        if(!this.symbolizer) {
            this.symbolizer = {};
        }        
        Ext.applyIf(this.symbolizer, this.defaultSymbolizer);

        this.haloCache = {};
		var mode = this.attributes.getCount() !== 0 ? "local" : "remote";
        var defAttributesComboConfig = {
            xtype: "combo",
            fieldLabel: this.labelValuesText,
            store: this.attributes,
            editable: false,
			mode: mode,
            triggerAction: "all",
            allowBlank: false,
            displayField: "name",
            valueField: "name",
            value: this.symbolizer.label && this.symbolizer.label.replace(/^\${(.*)}$/, "$1"),
            listeners: {
                select: function(combo, record) {
                    this.symbolizer.label = "${" + record.get("name") + "}";
                    this.fireEvent("change", this.symbolizer);
                },
                scope: this
            },
            width: 120
        };
        this.attributesComboConfig = this.attributesComboConfig || {};
        Ext.applyIf(this.attributesComboConfig, defAttributesComboConfig);
        
        this.labelWidth = 80;
        
        this.items = [this.attributesComboConfig, {
            cls: "x-html-editor-tb",
            style: "background: transparent; border: none; padding: 0 0em 0.5em;",
            xtype: "toolbar",
            items: [{
                xtype: "gxp_fontcombo",
                fonts: this.fonts || undefined,
                width: 110,
                value: this.symbolizer.fontFamily,
                listeners: {
                    select: function(combo, record) {
                        this.symbolizer.fontFamily = record.get("field1");
                        this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                }
            }, {
                xtype: "tbtext",
                text: this.sizeText + ": "
            }, {
                xtype: "numberfield",
                allowNegative: false,
                emptyText: OpenLayers.Renderer.defaultSymbolizer.fontSize,
                value: this.symbolizer.fontSize,
                width: 30,
                listeners: {
                    change: function(field, value) {
                        value = parseFloat(value);
                        if (isNaN(value)) {
                            delete this.symbolizer.fontSize;
                        } else {
                            this.symbolizer.fontSize = value;
                        }
                        this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                }
            }, {
                enableToggle: true,
                cls: "x-btn-icon",
                iconCls: "x-edit-bold",
                pressed: this.symbolizer.fontWeight === "bold",
                listeners: {
                    toggle: function(button, pressed) {
                        this.symbolizer.fontWeight = pressed ? "bold" : "normal";
                        this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                }
            }, {
                enableToggle: true,
                cls: "x-btn-icon",
                iconCls: "x-edit-italic",
                pressed: this.symbolizer.fontStyle === "italic",
                listeners: {
                    toggle: function(button, pressed) {
                        this.symbolizer.fontStyle = pressed ? "italic" : "normal";
                        this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                }
            }]
        }, {
            xtype: "gxp_fillsymbolizer",
            symbolizer: this.symbolizer,
            defaultColor: OpenLayers.Renderer.defaultSymbolizer.fontColor,
            checkboxToggle: false,
            autoHeight: true,
            width: 213,
            labelWidth: 70,
            plugins: this.colorManager && [new this.colorManager()],
            listeners: {
                change: function(symbolizer) {
                    this.fireEvent("change", this.symbolizer);
                },
                scope: this
            }
        }, {
            xtype: "fieldset",
            title: this.haloText,
            checkboxToggle: true,
            collapsed: !(this.symbolizer.haloRadius || this.symbolizer.haloColor || this.symbolizer.haloOpacity),
            autoHeight: true,
            labelWidth: 50,
            items: [{
                xtype: "numberfield",
                fieldLabel: this.sizeText,
                anchor: "89%",
                allowNegative: false,
                emptyText: OpenLayers.Renderer.defaultSymbolizer.haloRadius,
                value: this.symbolizer.haloRadius,
                listeners: {
                    change: function(field, value) {
                        value = parseFloat(value);
                        if (isNaN(value)) {
                            delete this.symbolizer.haloRadius;
                        } else {
                            this.symbolizer.haloRadius = value;
                        }
                        this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                }
            }, {
                xtype: "gxp_fillsymbolizer",
                symbolizer: {
                    fillColor: ("haloColor" in this.symbolizer) ? this.symbolizer.haloColor : OpenLayers.Renderer.defaultSymbolizer.haloColor,
                    fillOpacity: ("haloOpacity" in this.symbolizer) ? this.symbolizer.haloOpacity : OpenLayers.Renderer.defaultSymbolizer.haloOpacity
                },
                defaultColor: OpenLayers.Renderer.defaultSymbolizer.haloColor,
                checkboxToggle: false,
                width: 190,
                labelWidth: 60,
                plugins: this.colorManager && [new this.colorManager()],
                listeners: {
                    change: function(symbolizer) {
                        this.symbolizer.haloColor = symbolizer.fillColor;
                        this.symbolizer.haloOpacity = symbolizer.fillOpacity;
                        this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                }
            }],
            listeners: {
                collapse: function() {
                    this.haloCache = {
                        haloRadius: this.symbolizer.haloRadius,
                        haloColor: this.symbolizer.haloColor,
                        haloOpacity: this.symbolizer.haloOpacity
                    };
                    delete this.symbolizer.haloRadius;
                    delete this.symbolizer.haloColor;
                    delete this.symbolizer.haloOpacity;
                    this.fireEvent("change", this.symbolizer)
                },
                expand: function() {
                    Ext.apply(this.symbolizer, this.haloCache);
                    /**
                     * Start workaround for
                     * http://projects.opengeo.org/suite/ticket/676
                     */
                    this.doLayout();
                    /**
                     * End workaround for
                     * http://projects.opengeo.org/suite/ticket/676
                     */                    
                    this.fireEvent("change", this.symbolizer);
                },
                scope: this
            }
        }];

        this.addEvents(
            /**
             * Event: change
             * Fires before any field blurs if the field value has changed.
             *
             * Listener arguments:
             * symbolizer - {Object} A symbolizer with text related properties
             *     updated.
             */
            "change"
        ); 
 
        gxp.TextSymbolizer.superclass.initComponent.call(this);
        
    }
    
    
});

/** api: xtype = gxp_textsymbolizer */
Ext.reg('gxp_textsymbolizer', gxp.TextSymbolizer); 
/**
* Copyright (c) 2008-2011 The Open Planning Project
*
* Published under the GPL license.
* See https://github.com/opengeo/gxp/raw/master/license.txt for the full text
* of the license.
*/

/** 
 * @include widgets/FillSymbolizer.js
 * @include widgets/StrokeSymbolizer.js
 */

/** api: (define)
 *  module = gxp
 *  class = PolygonSymbolizer
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
Ext.namespace("gxp");

/** api: constructor
 *  .. class:: PolygonSymbolizer(config)
 *   
 *      Form for configuring a polygon symbolizer.
 */
gxp.PolygonSymbolizer = Ext.extend(Ext.Panel, {

    /** api: config[symbolizer]
     *  ``Object``
     *  A symbolizer object that will be used to fill in form values.
     *  This object will be modified when values change.  Clone first if
     *  you do not want your symbolizer modified.
     */
    symbolizer: null,

    initComponent: function() {
        
        this.items = [{
            xtype: "gxp_fillsymbolizer",
            symbolizer: this.symbolizer,
            listeners: {
                change: function(symbolizer) {
                    this.fireEvent("change", this.symbolizer);
                },
                scope: this
            }
        }, {
            xtype: "gxp_strokesymbolizer",
            symbolizer: this.symbolizer,
            listeners: {
                change: function(symbolizer) {
                    this.fireEvent("change", this.symbolizer);
                },
                scope: this
            }
        }];

        this.addEvents(
            /**
             * Event: change
             * Fires before any field blurs if the field value has changed.
             *
             * Listener arguments:
             * symbolizer - {Object} A symbolizer with stroke related properties
             *     updated.
             */
            "change"
        ); 

        gxp.PolygonSymbolizer.superclass.initComponent.call(this);

    }
    
    
});

/** api: xtype = gxp_linesymbolizer */
Ext.reg('gxp_polygonsymbolizer', gxp.PolygonSymbolizer);
/**
* Copyright (c) 2008-2011 The Open Planning Project
*
* Published under the GPL license.
* See https://github.com/opengeo/gxp/raw/master/license.txt for the full text
* of the license.
*/

/** api: (define)
 *  module = gxp.form
 *  class = ColorField
 *  base_link = `Ext.form.TextField <http://extjs.com/deploy/dev/docs/?class=Ext.form.TextField>`_
 */
Ext.namespace("gxp.form");

/** api: constructor
 *  .. class:: ColorField(config)
 *   
 *      A text field that colors its own background based on the input value.
 *      The value may be any one of the 16 W3C supported CSS color names
 *      (http://www.w3.org/TR/css3-color/).  The value can also be an arbitrary
 *      RGB hex value prefixed by a '#' (e.g. '#FFCC66').
 */
gxp.form.ColorField = Ext.extend(Ext.form.TextField,  {

    /** api: property[cssColors]
     *  ``Object``
     *  Properties are supported CSS color names.  Values are RGB hex strings
     *  (prefixed with '#').
     */
    cssColors: {
        aqua: "#00FFFF",
        black: "#000000",
        blue: "#0000FF",
        fuchsia: "#FF00FF",
        gray: "#808080",
        green: "#008000",
        lime: "#00FF00",
        maroon: "#800000",
        navy: "#000080",
        olive: "#808000",
        purple: "#800080",
        red: "#FF0000",
        silver: "#C0C0C0",
        teal: "#008080",
        white: "#FFFFFF",
        yellow: "#FFFF00"
    },
    
    /** api: config[defaultBackground]
     *  The default background color if the symbolizer has no fillColor set.
     *  Defaults to #ffffff.
     */
    defaultBackground: "#ffffff",

    /** private: method[initComponent]
     *  Override
     */
    initComponent: function() {
        if (this.value) {
            this.value = this.hexToColor(this.value);
        }
        gxp.form.ColorField.superclass.initComponent.call(this);
        
        // Add the colorField listener to color the field.
        this.on({
            render: this.colorField,
            valid: this.colorField,
            scope: this
        });
        
    },
    
    /** private: method[isDark]
     *  :arg hex: ``String`` A RGB hex color string (prefixed by '#').
     *  :returns: ``Boolean`` The color is dark.
     *  
     *  Determine if a color is dark by avaluating brightness according to the
     *  W3C suggested algorithm for calculating brightness of screen colors.
     *  http://www.w3.org/WAI/ER/WD-AERT/#color-contrast
     */
    isDark: function(hex) {
        var dark = false;
        if(hex) {
            // convert hex color values to decimal
            var r = parseInt(hex.substring(1, 3), 16) / 255;
            var g = parseInt(hex.substring(3, 5), 16) / 255;
            var b = parseInt(hex.substring(5, 7), 16) / 255;
            // use w3C brightness measure
            var brightness = (r * 0.299) + (g * 0.587) + (b * 0.144);
            dark = brightness < 0.5;
        }
        return dark;
    },
    
    /** private: method[colorField]
     *  Set the background and font color for the field.
     */
    colorField: function() {
        var color = this.colorToHex(this.getValue()) || this.defaultBackground;
        this.getEl().setStyle({
            "background": color,
            "color": this.isDark(color) ? "#ffffff" : "#000000"
        });
    },
    
    /** api: method[getHexValue]
     *  :returns: ``String`` The RGB hex string for the field's value (prefixed
     *      with '#').
     *  
     *  As a compliment to the field's ``getValue`` method, this method always
     *  returns the RGB hex string representation of the current value
     *  in the field (given a named color or a hex string).
     */
    getHexValue: function() {
        return this.colorToHex(
            gxp.form.ColorField.superclass.getValue.apply(this, arguments));
    },

    /** api: method[getValue]
     *  :returns: ``String`` The RGB hex string for the field's value (prefixed
     *      with '#').
     *  
     *  This method always returns the RGB hex string representation of the
     *  current value in the field (given a named color or a hex string),
     *  except for the case when the value has not been changed.
     */
    getValue: function() {
        var v = this.getHexValue();
        var o = this.initialConfig.value;
        if (v === this.hexToColor(o)) {
            v = o;
        }
        return v;
    },
    
    /** api: method[setValue]
     *  :arg value: ``Object``
     *  
     *  Sets the value of the field. If the value matches one of the well known
     *  colors in ``cssColors``, a human readable value will be displayed
     *  instead of the hex code.
     */
    setValue: function(value) {
        gxp.form.ColorField.superclass.setValue.apply(this,
            [this.hexToColor(value)]);
    },
    
    /** private: method[colorToHex]
     *  :returns: ``String`` A RGB hex color string or null if none found.
     *  
     *  Return the RGB hex representation of a color string.  If a CSS supported
     *  named color is supplied, the hex representation will be returned.
     *  If a non-CSS supported named color is supplied, null will be
     *  returned.  If a RGB hex string is supplied, the same will be returned.
     */
    colorToHex: function(color) {
        if (!color) {
            return color;
        }
        var hex;
        if (color.match(/^#[0-9a-f]{6}$/i)) {
            hex = color;
        } else {
            hex = this.cssColors[color.toLowerCase()] || null;
        }
        return hex;
    },
    
    /** private: method[hexToColor]
     */
    hexToColor: function(hex) {
        if (!hex) {
            return hex;
        }
        var color = hex;
        for (var c in this.cssColors) {
            if (this.cssColors[c] == color.toUpperCase()) {
                color = c;
                break;
            }
        }
        return color;
    }
    
});

Ext.reg("gxp_colorfield", gxp.form.ColorField);
/**
 * Copyright (c) 2008 The Open Planning Project
 */

/**
 * @require widgets/form/ColorField.js
 */

Ext.namespace("gxp");

/**
 * Class: gxp.ColorManager
 * A simple manager that handles the rendering of a ColorPicker window and
 *     coordinates the setting of values in a ColorField.  May be used as a
 *     plugin for a ColorField (or any text field that is intended to gather a
 *     RGB hex color value).
 */
gxp.ColorManager = function(config) {
    Ext.apply(this, config);
};

Ext.apply(gxp.ColorManager.prototype, {
    
    /**
     * Property: field
     * {<Styer.form.ColorField>} The currently focussed field.
     */
    field: null,
    
    /**
     * Method: init
     * Called when the color manager is used as a plugin.
     *
     * Parameters:
     * field - {<Styler.form.ColorField>} The field using this manager as a
     *     plugin.
     */
    init: function(field) {
        this.register(field);
    },
    
    /**
     * Method: destroy
     * Cleans up the manager.
     */
    destroy: function() {
        if(this.field) {
            this.unregister(this.field);
        }
    },
    
    
    /**
     * Method: register
     * Register a field with this manager.
     *
     * Parameters:
     * field - {<Styler.form.ColorField>} The field using this manager as a
     *     plugin.
     */
    register: function(field) {
        if(this.field) {
            this.unregister(this.field);
        }
        this.field = field;
        field.on({
            focus: this.fieldFocus,
            destroy: this.destroy,
            scope: this
        });
    },
    
    /**
     * Method: unregister
     * Unregister a field with this manager.
     * 
     * Parameters:
     * field - {<Styler.form.ColorField>} The field using this manager as a
     *     plugin.
     */
    unregister: function(field) {
        field.un("focus", this.fieldFocus, this);
        field.un("destroy", this.destroy, this);
        if(gxp.ColorManager.picker && field == this.field) {
            gxp.ColorManager.picker.un("pickcolor", this.setFieldValue, this);
        }
        this.field = null;
    },
    
    /**
     * Method: fieldFocus
     * Listener for "focus" event on a field.
     *
     * Parameters:
     * field - {<Styler.form.ColorField>} The focussed field.
     */
    fieldFocus: function(field) {
        if(!gxp.ColorManager.pickerWin) {
            gxp.ColorManager.picker = new Ext.ColorPalette();
            gxp.ColorManager.pickerWin = new Ext.Window({
                title: "Color Picker",
                closeAction: "hide",
                autoWidth: true,
                autoHeight: true
            });
        } else {
            gxp.ColorManager.picker.purgeListeners();
        }
        var listenerCfg = {
            select: this.setFieldValue,
            scope: this
        };
        var value = this.getPickerValue();
        if (value) {
            var colors = [].concat(gxp.ColorManager.picker.colors);
            if (!~colors.indexOf(value)) {
                if (gxp.ColorManager.picker.ownerCt) {
                    gxp.ColorManager.pickerWin.remove(gxp.ColorManager.picker);
                    gxp.ColorManager.picker = new Ext.ColorPalette();
                }
                colors.push(value);
                gxp.ColorManager.picker.colors = colors;
            }
            gxp.ColorManager.pickerWin.add(gxp.ColorManager.picker);
            gxp.ColorManager.pickerWin.doLayout();
            if (gxp.ColorManager.picker.rendered) {
                gxp.ColorManager.picker.select(value);
            } else {
                listenerCfg.afterrender = function() {
                    gxp.ColorManager.picker.select(value);
                };
            }
        }
        gxp.ColorManager.picker.on(listenerCfg);
        gxp.ColorManager.pickerWin.show();
    },
    
    /**
     * Method: setFieldValue
     * Listener for the "pickcolor" event of the color picker.  Only sets the
     *     field value if the field is visible.
     *
     * Parameters:
     * picker - {Ext.ux.ColorPicker} The color picker
     * color - {String} The RGB hex value (not prefixed with "#")
     */
    setFieldValue: function(picker, color) {
        if(this.field.isVisible()) {
            this.field.setValue("#" + color);
        }
    },
    
    /**
     * Method: getPickerValue
     */
    getPickerValue: function() {
        var field = this.field;
        var hex = field.getHexValue ?
            (field.getHexValue() || field.defaultBackground) :
            field.getValue();
        if (hex) {
            return hex.substr(1);
        }
    }
    
});

(function() {
    // register the color manager with every color field
    Ext.util.Observable.observeClass(gxp.form.ColorField);
    gxp.form.ColorField.on({
        render: function(field) {
            var manager = new gxp.ColorManager();
            manager.register(field);
        }
    });
})();

gxp.ColorManager.picker = null;

gxp.ColorManager.pickerWin = null;/**
* Copyright (c) 2008-2011 The Open Planning Project
*
* Published under the GPL license.
* See https://github.com/opengeo/gxp/raw/master/license.txt for the full text
* of the license.
*/
 
/**
 * @include widgets/form/ColorField.js
 */

/** api: (define)
 *  module = gxp
 *  class = FillSymbolizer
 *  base_link = `Ext.FormPanel <http://extjs.com/deploy/dev/docs/?class=Ext.FormPanel>`_
 */
Ext.namespace("gxp");

/** api: constructor
 *  .. class:: FillSymbolizer(config)
 *   
 *      Form for configuring a symbolizer fill.
 */
gxp.FillSymbolizer = Ext.extend(Ext.FormPanel, {
    
    /** api: config[symbolizer]
     *  ``Object``
     *  A symbolizer object that will be used to fill in form values.
     *  This object will be modified when values change.  Clone first if
     *  you do not want your symbolizer modified.
     */
    symbolizer: null,
    
    /** api: config[colorManager]
     *  ``Function``
     *  Optional color manager constructor to be used as a plugin for the color
     *  field.
     */
    colorManager: null,
    
    /** api: config[checkboxToggle]
     *  ``Boolean`` Set to false if the "Fill" fieldset should not be
     *  toggleable. Default is true.
     */
    checkboxToggle: true,
    
    /** api: config[defaultColor]
     *  ``String`` Default background color for the Color field. This
     *  color will be displayed when no fillColor value for the symbolizer
     *  is available. Defaults to the ``fillColor`` property of
     *  ``OpenLayers.Renderer.defaultSymbolizer``.
     */
    defaultColor: null,

    border: false,
    
    /** i18n */
    fillText: "Fill",
    colorText: "Color",
    opacityText: "Opacity",
    
    initComponent: function() {
        
        if(!this.symbolizer) {
            this.symbolizer = {};
        }
        
        var colorFieldPlugins;
        if (this.colorManager) {
            colorFieldPlugins = [new this.colorManager()];
        }
        
        this.items = [{
            xtype: "fieldset",
            title: this.fillText,
            autoHeight: true,
            checkboxToggle: this.checkboxToggle,
            collapsed: this.checkboxToggle === true &&
                this.symbolizer.fill === false,
            hideMode: "offsets",
            defaults: {
                width: 100 // TODO: move to css
            },
            items: [{
                xtype: "gxp_colorfield",
                fieldLabel: this.colorText,
                name: "color",
                emptyText: OpenLayers.Renderer.defaultSymbolizer.fillColor,
                value: this.symbolizer.fillColor,
                defaultBackground: this.defaultColor ||
                    OpenLayers.Renderer.defaultSymbolizer.fillColor,
                plugins: colorFieldPlugins,
                listeners: {
                    valid: function(field) {
                        var newValue = field.getValue();
                        var modified = this.symbolizer.fillColor != newValue; 
                        this.symbolizer.fillColor = newValue;
                        modified && this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                }
            }, {
                xtype: "slider",
                fieldLabel: this.opacityText,
                name: "opacity",
                values: [(("fillOpacity" in this.symbolizer) ? this.symbolizer.fillOpacity : OpenLayers.Renderer.defaultSymbolizer.fillOpacity) * 100],
                isFormField: true,
                listeners: {
                    changecomplete: function(slider, value) {
                        this.symbolizer.fillOpacity = value / 100;
                        this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                },
                plugins: [
                    new GeoExt.SliderTip({
                        getText: function(thumb) {
                            return thumb.value + "%";
                        }
                    })
                ]
            }],
            listeners: {
                "collapse": function() {
                    if (this.symbolizer.fill !== false) {
                        this.symbolizer.fill = false;
                        this.fireEvent("change", this.symbolizer);
                    }
                },
                "expand": function() {
                    this.symbolizer.fill = true;
                    this.fireEvent("change", this.symbolizer);
                },
                scope: this
            }
        }];

        this.addEvents(
            /**
             * Event: change
             * Fires before any field blurs if the field value has changed.
             *
             * Listener arguments:
             * symbolizer - {Object} A symbolizer with fill related properties
             *     updated.
             */
            "change"
        ); 

        gxp.FillSymbolizer.superclass.initComponent.call(this);
        
    }
    
    
});

/** api: xtype = gxp_fillsymbolizer */
Ext.reg('gxp_fillsymbolizer', gxp.FillSymbolizer);
/**
* Copyright (c) 2008-2011 The Open Planning Project
*
* Published under the GPL license.
* See https://github.com/opengeo/gxp/raw/master/license.txt for the full text
* of the license.
*/

/**
 * @include widgets/form/ColorField.js
 */

/** api: (define)
 *  module = gxp
 *  class = StrokeSymbolizer
 *  base_link = `Ext.FormPanel <http://extjs.com/deploy/dev/docs/?class=Ext.FormPanel>`_
 */
Ext.namespace("gxp");

/** api: constructor
 *  .. class:: StrokeSymbolizer(config)
 *   
 *      Form for configuring a symbolizer stroke.
 */
gxp.StrokeSymbolizer = Ext.extend(Ext.FormPanel, {
    
    /* i18n */
    solidStrokeName: "solid",
    dashStrokeName: "dash",
    dotStrokeName: "dot",
    titleText: "Stroke",
    styleText: "Style",
    colorText: "Color",
    widthText: "Width",
    opacityText: "Opacity",
    /* ~i18n */
    
    /** api: config[symbolizer]
     *  ``Object``
     *  A symbolizer object that will be used to fill in form values.
     *  This object will be modified when values change.  Clone first if
     *  you do not want your symbolizer modified.
     */
    symbolizer: null,
    
    /** api: config[colorManager]
     *  ``Function``
     *  Optional color manager constructor to be used as a plugin for the color
     *  field.
     */
    colorManager: null,
    
    /** api: config[checkboxToggle]
     *  ``Boolean`` Set to false if the "Fill" fieldset should not be
     *  toggleable. Default is true.
     */
    checkboxToggle: true,
    
    /** api: config[defaultColor]
     *  ``String`` Default background color for the Color field. This
     *  color will be displayed when no strokeColor value for the symbolizer
     *  is available. Defaults to the ``strokeColor`` property of
     *  ``OpenLayers.Renderer.defaultSymbolizer``.
     */
    defaultColor: null,

    /** api: config[dashStyles]
     *  ``Array(Array)``
     *  A list of [value, name] pairs for stroke dash styles.
     *  The first item in each list is the value and the second is the
     *  display name.  Default is [["solid", "solid"], ["2 4", "dash"],
     *  ["1 4", "dot"]].
     */
    dashStyles: null,
    
    border: false,
    
    initComponent: function() {
        
        this.dashStyles = this.dashStyles || [["solid", this.solidStrokeName], ["4 4", this.dashStrokeName], ["2 4", this.dotStrokeName]];
        
        if(!this.symbolizer) {
            this.symbolizer = {};
        }
        
        var colorFieldPlugins;
        if (this.colorManager) {
            colorFieldPlugins = [new this.colorManager];
        }

        this.items = [{
            xtype: "fieldset",
            title: this.titleText,
            autoHeight: true,
            checkboxToggle: this.checkboxToggle,
            collapsed: this.checkboxToggle === true &&
                this.symbolizer.stroke === false,
            hideMode: "offsets",
            defaults: {
                width: 100 // TODO: move to css
            },
            items: [{
                xtype: "combo",
                name: "style",
                fieldLabel: this.styleText,
                store: new Ext.data.SimpleStore({
                    data: this.dashStyles,
                    fields: ["value", "display"]
                }),
                displayField: "display",
                valueField: "value",
                value: this.getDashArray(this.symbolizer.strokeDashstyle) || OpenLayers.Renderer.defaultSymbolizer.strokeDashstyle,
                mode: "local",
                allowBlank: true,
                triggerAction: "all",
                editable: false,
                listeners: {
                    select: function(combo, record) {
                        this.symbolizer.strokeDashstyle = record.get("value");
                        this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                }
            }, {
                xtype: "gxp_colorfield",
                name: "color",
                fieldLabel: this.colorText,
                emptyText: OpenLayers.Renderer.defaultSymbolizer.strokeColor,
                value: this.symbolizer.strokeColor,
                defaultBackground: this.defaultColor ||
                    OpenLayers.Renderer.defaultSymbolizer.strokeColor,
                plugins: colorFieldPlugins,
                listeners: {
                    valid: function(field) {
                        var newValue = field.getValue();
                        var modified = this.symbolizer.strokeColor != newValue;
                        this.symbolizer.strokeColor = newValue;
                        modified && this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                }
            }, {
                xtype: "numberfield",
                name: "width",
                fieldLabel: this.widthText,
                allowNegative: false,
                emptyText: OpenLayers.Renderer.defaultSymbolizer.strokeWidth,
                value: this.symbolizer.strokeWidth,
                listeners: {
                    change: function(field, value) {
                        value = parseFloat(value);
                        if (isNaN(value)) {
                            delete this.symbolizer.strokeWidth;
                        } else {
                            this.symbolizer.strokeWidth = value;
                        }
                        this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                }
            }, {
                xtype: "slider",
                name: "opacity",
                fieldLabel: this.opacityText,
                values: [(("strokeOpacity" in this.symbolizer) ? this.symbolizer.strokeOpacity : OpenLayers.Renderer.defaultSymbolizer.strokeOpacity) * 100],
                isFormField: true,
                listeners: {
                    changecomplete: function(slider, value) {
                        this.symbolizer.strokeOpacity = value / 100;
                        this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                },
                plugins: [
                    new GeoExt.SliderTip({
                        getText: function(thumb) {
                            return thumb.value + "%";
                        }
                    })
                ]
            }],
            listeners: {
                "collapse": function() {
                    if (this.symbolizer.stroke !== false) {
                        this.symbolizer.stroke = false;
                        this.fireEvent("change", this.symbolizer);
                    }
                },
                "expand": function() {
                    this.symbolizer.stroke = true;
                    this.fireEvent("change", this.symbolizer);
                },
                scope: this
            }
        }];

        this.addEvents(
            /**
             * Event: change
             * Fires before any field blurs if the field value has changed.
             *
             * Listener arguments:
             * symbolizer - {Object} A symbolizer with stroke related properties
             *     updated.
             */
            "change"
        ); 
 
        gxp.StrokeSymbolizer.superclass.initComponent.call(this);
        
    },

    getDashArray: function(style) {
        var array;
        if (style) {
            var parts = style.split(/\s+/);
            var ratio = parts[0] / parts[1];
            var array;
            if (!isNaN(ratio)) {
                array = ratio >= 1 ? "4 4" : "2 4";
            }
        }
        return array;
    }
    
    
    
});

/** api: xtype = gxp_strokesymbolizer */
Ext.reg('gxp_strokesymbolizer', gxp.StrokeSymbolizer); 
/**
* Copyright (c) 2008-2011 The Open Planning Project
*
* Published under the GPL license.
* See https://github.com/opengeo/gxp/raw/master/license.txt for the full text
* of the license.
*/

/*
 * @include widgets/StrokeSymbolizer.js
 */

/** api: (define)
 *  module = gxp
 *  class = LineSymbolizer
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
Ext.namespace("gxp");

/** api: constructor
 *  .. class:: LineSymbolizer(config)
 *   
 *      Form for configuring a line symbolizer.
 */
gxp.LineSymbolizer = Ext.extend(Ext.Panel, {

    /** api: config[symbolizer]
     *  ``Object``
     *  A symbolizer object that will be used to fill in form values.
     *  This object will be modified when values change.  Clone first if
     *  you do not want your symbolizer modified.
     */
    symbolizer: null,

    initComponent: function() {
        
        this.items = [{
            xtype: "gxp_strokesymbolizer",
            symbolizer: this.symbolizer,
            listeners: {
                change: function(symbolizer) {
                    this.fireEvent("change", this.symbolizer);
                },
                scope: this
            }
        }];

        this.addEvents(
            /**
             * Event: change
             * Fires before any field blurs if the field value has changed.
             *
             * Listener arguments:
             * symbolizer - {Object} A symbolizer with stroke related properties
             *     updated.
             */
            "change"
        ); 

        gxp.LineSymbolizer.superclass.initComponent.call(this);

    }
    
    
});

/** api: xtype = gxp_linesymbolizer */
Ext.reg('gxp_linesymbolizer', gxp.LineSymbolizer);
/**
* Copyright (c) 2008-2011 The Open Planning Project
*
* Published under the GPL license.
* See https://github.com/opengeo/gxp/raw/master/license.txt for the full text
* of the license.
*/

/** 
 * @include widgets/FillSymbolizer.js
 * @include widgets/StrokeSymbolizer.js
 */

/** api: (define)
 *  module = gxp
 *  class = PointSymbolizer
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
Ext.namespace("gxp");

/** api: constructor
 *  .. class:: PointSymbolizer(config)
 *   
 *      Form for configuring a point symbolizer.
 */
gxp.PointSymbolizer = Ext.extend(Ext.Panel, {

    /** api: config[symbolizer]
     *  ``Object``
     *  A symbolizer object that will be used to fill in form values.
     *  This object will be modified when values change.  Clone first if
     *  you do not want your symbolizer modified.
     */
    symbolizer: null,
    
    /** i18n */
    graphicCircleText: "circle",
    graphicSquareText: "square",
    graphicTriangleText: "triangle",
    graphicStarText: "star",
    graphicCrossText: "cross",
    graphicXText: "x",
    graphicExternalText: "external",
    urlText: "URL",
    opacityText: "opacity",
    symbolText: "Symbol",
    sizeText: "Size",
    rotationText: "Rotation",
    
    /** api: config[pointGraphics]
     *  ``Array``
     *  A list of objects to be used as the root of the data for a
     *  JsonStore.  These will become records used in the selection of
     *  a point graphic.  If an object in the list has no "value" property,
     *  the user will be presented with an input to provide their own URL
     *  for an external graphic.  By default, names of well-known marks are
     *  provided.  In addition, the default list will produce a record with
     *  display of "external" that create an input for an external graphic
     *  URL.
     *
     * Fields:
     *
     *  * display - ``String`` The name to be displayed to the user.
     *  * preview - ``String`` URL to a graphic for preview.
     *  * value - ``String`` Value to be sent to the server.
     *  * mark - ``Boolean`` The value is a well-known name for a mark.  If
     *      ``false``, the value will be assumed to be a url for an external graphic.
     */
    pointGraphics: null,
    
   /** api: config[colorManager]
     *  ``Function``
     *  Optional color manager constructor to be used as a plugin for the color
     *  field.
     */
    colorManager: null,
    
    /** private: property[external]
     *  ``Boolean``
     *  Currently using an external graphic.
     */
    external: null,
    
    /** private: config[layout]
     *  ``String``
     */
    layout: "form",

    initComponent: function() {
        
        if(!this.symbolizer) {
            this.symbolizer = {};
        }   
        
        if (!this.pointGraphics) {
            this.pointGraphics = [
                {display: this.graphicCircleText, value: "circle", mark: true},
                {display: this.graphicSquareText, value: "square", mark: true},
                {display: this.graphicTriangleText, value: "triangle", mark: true},
                {display: this.graphicStarText, value: "star", mark: true},
                {display: this.graphicCrossText, value: "cross", mark: true},
                {display: this.graphicXText, value: "x", mark: true},
                {display: this.graphicExternalText}
            ];
        }
        
        this.external = !!this.symbolizer["externalGraphic"];

        this.markPanel = new Ext.Panel({
            border: false,
            collapsed: this.external,
            layout: "form",
            items: [{
                xtype: "gxp_fillsymbolizer",
                symbolizer: this.symbolizer,
                labelWidth: this.labelWidth,
                labelAlign: this.labelAlign,
                colorManager: this.colorManager,
                listeners: {
                    change: function(symbolizer) {
                        this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                }
            }, {
                xtype: "gxp_strokesymbolizer",
                symbolizer: this.symbolizer,
                labelWidth: this.labelWidth,
                labelAlign: this.labelAlign,
                colorManager: this.colorManager,
                listeners: {
                    change: function(symbolizer) {
                        this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                }
            }]
        });
        
        this.urlField = new Ext.form.TextField({
            name: "url",
            fieldLabel: this.urlText,
            value: this.symbolizer["externalGraphic"],
            hidden: true,
            listeners: {
                change: function(field, value) {
                    this.symbolizer["externalGraphic"] = value;
                    this.fireEvent("change", this.symbolizer);
                },
                scope: this
            },
            width: 100 // TODO: push this to css
        });
        
        this.graphicPanel = new Ext.Panel({
            border: false,
            collapsed: !this.external,
            layout: "form",
            items: [this.urlField, {
                xtype: "slider",
                name: "opacity",
                fieldLabel: this.opacityText,
                value: [(this.symbolizer["graphicOpacity"] == null) ? 100 : this.symbolizer["graphicOpacity"] * 100],
                isFormField: true,
                listeners: {
                    changecomplete: function(slider, value) {
                        this.symbolizer["graphicOpacity"] = value / 100;
                        this.fireEvent("change", this.symbolizer);
                    },
                    scope: this
                },
                plugins: [
                    new GeoExt.SliderTip({
                        getText: function(thumb) {
                            return thumb.value + "%";
                        }
                    })
                ],
                width: 100 // TODO: push this to css                
            }]
        });

        this.items = [{
            xtype: "combo",
            name: "mark",
            fieldLabel: this.symbolText,
            store: new Ext.data.JsonStore({
                data: {root: this.pointGraphics},
                root: "root",
                fields: ["value", "display", "preview", {name: "mark", type: "boolean"}]
            }),
            value: this.external ? 0 : this.symbolizer["graphicName"],
            displayField: "display",
            valueField: "value",
            tpl: new Ext.XTemplate(
                '<tpl for=".">' +
                    '<div class="x-combo-list-item gx-pointsymbolizer-mark-item">' +
                    '<tpl if="preview">' +
                        '<img src="{preview}" alt="{display}"/>' +
                    '</tpl>' +
                    '<span>{display}</span>' +
                '</div></tpl>'
            ),
            mode: "local",
            allowBlank: false,
            triggerAction: "all",
            editable: false,
            listeners: {
                select: function(combo, record) {
                    var mark = record.get("mark");
                    var value = record.get("value");
                    if(!mark) {
                        if(value) {
                            this.urlField.hide();
                            // this to hide the container - otherwise the label remains
                            this.urlField.getEl().up('.x-form-item').setDisplayed(false);
                            this.symbolizer["externalGraphic"] = value;
                        } else {
                            this.urlField.show();
                            this.urlField.getEl().up('.x-form-item').setDisplayed(true);
                        }
                        if(!this.external) {
                            this.external = true;
                            this.updateGraphicDisplay();
                        }
                    } else {
                        if(this.external) {
                            this.external = false;
                            delete this.symbolizer["externalGraphic"];
                            this.updateGraphicDisplay();
                        }
                        this.symbolizer["graphicName"] = value;
                    }
                    this.fireEvent("change", this.symbolizer);
                },
                scope: this
            },
            width: 100 // TODO: push this to css
        }, {
            xtype: "textfield",
            name: "size",
            fieldLabel: this.sizeText,
            value: this.symbolizer["pointRadius"] && this.symbolizer["pointRadius"] * 2,
            listeners: {
                change: function(field, value) {
                    this.symbolizer["pointRadius"] = value / 2;
                    this.fireEvent("change", this.symbolizer);
                },
                scope: this
            },
            width: 100 // TODO: push this to css
        }, {
            xtype: "textfield",
            name: "rotation",
            fieldLabel: this.rotationText,
            value: this.symbolizer["rotation"],
            listeners: {
                change: function(field, value) {
                    this.symbolizer["rotation"] = value;
                    this.fireEvent("change", this.symbolizer);
                },
                scope: this
            },
            width: 100 // TODO: push this to css
        }, this.markPanel, this.graphicPanel
        ];

        this.addEvents(
            /**
             * Event: change
             * Fires before any field blurs if the field value has changed.
             *
             * Listener arguments:
             * symbolizer - {Object} A symbolizer with stroke related properties
             *     updated.
             */
            "change"
        ); 

        gxp.PointSymbolizer.superclass.initComponent.call(this);

    },
    
    updateGraphicDisplay: function() {
        if(this.external) {
            this.markPanel.collapse();
            this.graphicPanel.expand();
        } else {
            this.graphicPanel.collapse();
            this.markPanel.expand();
        }
        // TODO: window shadow fails to sync
    }
    
    
});

/** api: xtype = gxp_pointsymbolizer */
Ext.reg('gxp_pointsymbolizer', gxp.PointSymbolizer);
/**
* Copyright (c) 2008-2011 The Open Planning Project
*
* Published under the GPL license.
* See https://github.com/opengeo/gxp/raw/master/license.txt for the full text
* of the license.
*/

/** api: (define)
 *  module = gxp.form
 *  class = ComparisonComboBox
 *  base_link = `Ext.form.ComboBox <http://extjs.com/deploy/dev/docs/?class=Ext.form.ComboBox>`_
 */
Ext.namespace("gxp.form");

/** api: constructor
 *  .. class:: ComparisonComboBox(config)
 *   
 *      A combo box for selecting comparison operators available in OGC
 *      filters.
 */
gxp.form.ComparisonComboBox = Ext.extend(Ext.form.ComboBox, {
    
    allowedTypes: [
        [OpenLayers.Filter.Comparison.EQUAL_TO, "="],
        [OpenLayers.Filter.Comparison.NOT_EQUAL_TO, "<>"],
        [OpenLayers.Filter.Comparison.LESS_THAN, "<"],
        [OpenLayers.Filter.Comparison.GREATER_THAN, ">"],
        [OpenLayers.Filter.Comparison.LESS_THAN_OR_EQUAL_TO, "<="],
        [OpenLayers.Filter.Comparison.GREATER_THAN_OR_EQUAL_TO, ">="],
        [OpenLayers.Filter.Comparison.LIKE, "like"],
        // simulate ilike operator (not match case)
        ["ilike", "ilike"],
        [OpenLayers.Filter.Comparison.BETWEEN, "between"]
    ],

    allowBlank: false,

    mode: "local",

    typeAhead: true,

    forceSelection: true,

    triggerAction: "all",

    width: 50,

    editable: true,
  
    initComponent: function() {
        var defConfig = {
            displayField: "name",
            valueField: "value",
            store: new Ext.data.SimpleStore({
                data: this.allowedTypes,
                fields: ["value", "name"]
            }),
            value: (this.value === undefined) ? this.allowedTypes[0][0] : this.value,
            listeners: {
                // workaround for select event not being fired when tab is hit
                // after field was autocompleted with forceSelection
                "blur": function() {
                    var index = this.store.findExact("value", this.getValue());
                    if (index != -1) {
                        this.fireEvent("select", this, this.store.getAt(index));
                    } else if (this.startValue != null) {
                        this.setValue(this.startValue);
                    }
                }
            }
        };
        Ext.applyIf(this, defConfig);
        
        gxp.form.ComparisonComboBox.superclass.initComponent.call(this);
    }
});

Ext.reg("gxp_comparisoncombo", gxp.form.ComparisonComboBox);/**
* Copyright (c) 2014 Geosolutions
*
* Published under the GPL license.
* See https://github.com/opengeo/gxp/raw/master/license.txt for the full text
* of the license.
*/


/** api: (define)
 *  module = gxp.widgets.form
 *  class = WPSUniqueValuesCombo
 *  base_link = `Ext.form.ComboBox <http://extjs.com/deploy/dev/docs/?class=Ext.form.ComboBox>`_
 */
Ext.namespace("gxp.widgets.form");

/** api: constructor
 *  .. class:: WPSUniqueValuesCombo(config)
 *   
 *      A combo box targeted to show unique values for a given field and layer.
 *      It accepts all the config values of ComboBox.
 */
gxp.widgets.form.WPSUniqueValuesCombo = Ext.extend(Ext.form.ComboBox, {
    // private
    getParams : function(q){
        var params = this.superclass().getParams.call(this, q);
        Ext.apply(params, this.store.baseParams);
        return params;
    },
    // private
    initList : function() { // warning: overriding ComboBox private method
        this.superclass().initList.call(this);
        if (this.pageTb && this.pageTb instanceof Ext.PagingToolbar) {
            this.pageTb.afterPageText = "";
            this.pageTb.beforePageText = "";
            this.pageTb.displayMsg = "";
        }
    }
});
Ext.reg('gxp_wpsuniquevaluescb', gxp.widgets.form.WPSUniqueValuesCombo);/**
 * Copyright (c) 2008-2011 The Open Planning Project
 * 
 * Published under the GPL license.
 * See https://github.com/opengeo/gxp/raw/master/license.txt for the full text
 * of the license.
 */

/**
 * @include widgets/form/ComparisonComboBox.js
 * @include data/WPSUniqueValuesReader.js
 * @include data/WPSUniqueValuesProxy.js
 * @include data/WPSUniqueValuesStore.js
 * @include widgets/form/WPSUniqueValuesCombo.js
 * requires GeoExt/data/AttributeStore.js
 */

/** api: (define)
 *  module = gxp.form
 *  class = FilterField
 *  base_link = `Ext.form.CompositeField <http://extjs.com/deploy/dev/docs/?class=Ext.form.CompositeField>`_
 */
Ext.namespace("gxp.form");

/** api: constructor
 *  .. class:: FilterField(config)
 *   
 *      A form field representing a comparison filter.
 */
gxp.form.FilterField = Ext.extend(Ext.form.CompositeField, {
    
    /** api:config[lowerBoundaryTip]
     *  ``String`` tooltip for the lower boundary textfield (i18n)
     */
    lowerBoundaryTip: "lower boundary",
     
    /** api:config[upperBoundaryTip]
     *  ``String`` tooltip for the lower boundary textfield (i18n)
     */
    upperBoundaryTip: "upper boundary",
     
    /** api: config[caseInsensitiveMatch]
     *  ``Boolean``
     *  Should Comparison Filters for Strings do case insensitive matching?  Default is ``"false"``.
     */
    caseInsensitiveMatch: false,

    /**
     * Property: filter
     * {OpenLayers.Filter} Optional non-logical filter provided in the initial
     *     configuration.  To retreive the filter, use <getFilter> instead
     *     of accessing this property directly.
     */
    filter: null,
    
    /**
     * Property: attributes
     * {GeoExt.data.AttributeStore} A configured attributes store for use in
     *     the filter property combo.
     */
    attributes: null,

    /** api:config[comparisonComboConfig]
     *  ``Object`` Config object for comparison combobox.
     */

    /** api:config[attributesComboConfig]
     *  ``Object`` Config object for attributes combobox.
     */
    
    /**
     * Property: attributesComboConfig
     * {Object}
     */
    attributesComboConfig: null,
    
    /** api:config[autoComplete]
     *  ``Boolean`` autocomplete enabled on text fields.
     */
    autoComplete: false,
    
    /** api:config[autoCompleteCfg]
     *  ``Object`` autocomplete configuration object.
     */
    autoCompleteCfg: {},
    
    /**
     * 
     */ 
    uniqueValuesStore : null,
    
    valid: null,
    
    pageSize: 5,
    
    addValidation: function(config) {
        //Add VTYPE validation according to validators config
        var feature = (this.attributes.baseParams && this.attributes.baseParams.TYPENAME) ? this.attributes.baseParams.TYPENAME.split(":")[1] : 'feature';
        var fieldName = this.filter.property;
        if(this.validators && this.validators[feature] && this.validators[feature][fieldName]) {
            var validator = this.validators[feature][fieldName];
            // currently we support only regex based validation
            if(validator.type === 'regex') {
                var valueTest = new RegExp(validator.value);
                
                Ext.apply(config,{
                    validator: function(value) {
                        return valueTest.test(value) ? true : validator.invalidText;
                    }
                });
            }
        }
        return config;
    },
    
    addAutocompleteStore: function(config) {
        var uniqueValuesStore = new gxp.data.WPSUniqueValuesStore({
            pageSize: this.autoCompleteCfg.pageSize || this.pageSize
        });
        
        this.initUniqueValuesStore(uniqueValuesStore, this.autoCompleteCfg.url || this.attributes.url, this.attributes.baseParams.TYPENAME, this.attributes.format.namespaces, this.filter.property);
        
        return Ext.apply(Ext.apply({}, config), {store: uniqueValuesStore});
    },
    
    createValueWidget: function(type, value) {
        if(this.autoComplete && this.fieldType === 'string') {
            return Ext.apply({value:value}, this.addAutocompleteStore(this.autoCompleteDefault[type]));
        } else {
            return Ext.apply({value:value}, this.fieldDefault[type][this.fieldType]);
        }
    },
    
    createValueWidgets: function(type, value, force) {
        if(type !== this.filter.type || force) {
            this.setFilterType(type);
            
            if(!this.valueWidgets) {
                this.valueWidgets = this.items.get(2);
            }
            this.valueWidgets.removeAll();
            if (type === OpenLayers.Filter.Comparison.BETWEEN) {
                this.valueWidgets.add(this.addValidation(this.createValueWidget('lower'), value[0]));
                this.valueWidgets.add(this.addValidation(this.createValueWidget('upper'), value[1]));
            } else {
                this.valueWidgets.add(this.addValidation(this.createValueWidget('single', value[0])));
            }
            
            this.doLayout();
            
            this.fireEvent("change", this.filter, this);
        }
    },
    
    createDefaultConfigs: function() {
        this.defaultItemsProp = {
            'single': {
                validateOnBlur: false,
                ref: "value",
                //value: this.filter.value,
                width: 50,
                grow: true,
                growMin: 50,
                anchor: "100%",
                allowBlank: this.allowBlank,
                listeners: {
                    "change": function(field, value) {
                        this.filter.value = value;
                        this.fireEvent("change", this.filter, this);
                    },
                    "blur": function(field){
                    
                    },
                    scope: this
                }   
            },
            
           'lower': {
                //value: this.filter.lowerBoundary,
                //tooltip: this.lowerBoundaryTip,
                grow: true,
                growMin: 30,
                width: 30,
                ref: "lowerBoundary",
                anchor: "100%",
                allowBlank: this.allowBlank,
                listeners: {
                    "change": function(field, value) {
                        this.filter.lowerBoundary = value;
                        this.fireEvent("change", this.filter, this);
                    },
                    "autosize": function(field, width) {
                        field.setWidth(width);
                        field.ownerCt.doLayout();
                    },
                    scope: this
                }
            },
            
            'upper': {
                grow: true,
                growMin: 30,
                width: 30,
                ref: "upperBoundary",
                allowBlank: this.allowBlank,
                listeners: {
                    "change": function(field, value) {
                        this.filter.upperBoundary = value;
                        this.fireEvent("change", this.filter, this);
                    },

                    scope: this
                }
            }
        };
        
        this.fieldDefault = {};
        
        for(key in this.defaultItemsProp) {
            this.fieldDefault[key] = {
                'string': Ext.applyIf({
                    xtype: "textfield"
                }, this.defaultItemsProp[key]),
                'double': Ext.applyIf({
                    xtype: "numberfield",
                    allowDecimals:true,
                    decimalPrecision: 10
                },this.defaultItemsProp[key]),
                'float': Ext.applyIf({
                    xtype: "numberfield",
                    allowDecimals:true,
                    decimalPrecision: 10
                },this.defaultItemsProp[key]),
                'decimal': Ext.applyIf({
                    xtype: "numberfield",
                    allowDecimals:true,
                    decimalPrecision: 10
                },this.defaultItemsProp[key]),
                'int': Ext.applyIf({
                    xtype: "numberfield",
                    allowDecimals:false
                },this.defaultItemsProp[key]),
                'integer': Ext.applyIf({
                    xtype: "numberfield",
                    allowDecimals:false
                },this.defaultItemsProp[key]),
                'long': Ext.applyIf({
                    xtype: "numberfield",
                    allowDecimals:false
                },this.defaultItemsProp[key]),
                'short': Ext.applyIf({
                    xtype: "numberfield",
                    allowDecimals:false
                },this.defaultItemsProp[key]),
                'date': Ext.applyIf({
                    xtype: "datefield",
                    width: 70,
                    allowBlank: false,
                    format: this.dateFormat
                },this.defaultItemsProp[key]),
                'dateTime': Ext.applyIf({
                    xtype: "datefield",
                    width: 70,
                    allowBlank: false,
                    format: this.dateFormat
                },this.defaultItemsProp[key])
            };
        }
        
        this.autoCompleteDefault = {
        
            'single': Ext.applyIf({
                xtype: "gxp_wpsuniquevaluescb",
                mode: "remote", // required as the combo store shouldn't be loaded before a field name is selected
                //store: this.uniqueValuesStore,
                pageSize: this.autoCompleteCfg.pageSize || this.pageSize,
                typeAhead: false,
                forceSelection: false,
                remoteSort: true,
                triggerAction: "all",
                allowBlank: this.allowBlank,
                displayField: "value",
                valueField: "value",
                minChars: 1,
                resizable: true,
                listeners: {
                    select: function(combo, record) {
                        this.filter.value = combo.getValue();
                        this.fireEvent("change", this.filter);
                    },
                    blur: function(combo) {
                        this.filter.value = combo.getValue();
                        this.fireEvent("change", this.filter);
                    },
                    beforequery: function(evt) {
                        evt.combo.store.baseParams.start = 0;
                    },
                    scope: this
                },
                width: 80,
                listWidth: 250,
                grow: true,
                growMin: 50,
                anchor: "100%"
            },this.defaultItemsProp['single']),
            'lower': Ext.applyIf({
                xtype: "gxp_wpsuniquevaluescb",
                mode: "remote", // required as the combo store shouldn't be loaded before a field name is selected
                //store: this.uniqueValuesStore,
                pageSize: this.autoCompleteCfg.pageSize || this.pageSize,
                typeAhead: false,
                forceSelection: false,
                remoteSort: true,
                triggerAction: "all",
                allowBlank: this.allowBlank,
                displayField: "value",
                valueField: "value",
                minChars: 1,
                resizable: true,
                listeners: {
                    select: function(combo, record) {
                        this.filter.lowerBoundary = combo.getValue();
                        this.fireEvent("change", this.filter);
                    },
                    blur: function(combo) {
                        this.filter.lowerBoundary = combo.getValue();
                        this.fireEvent("change", this.filter);
                    },
                    beforequery: function(evt) {
                        evt.combo.store.baseParams.start = 0;
                    },
                    scope: this
                },
                width: 50,
                listWidth: 250,
                grow: true,
                growMin: 50,
                anchor: "100%"
            },this.defaultItemsProp['lower']),
            'upper': Ext.applyIf({
                xtype: "gxp_wpsuniquevaluescb",
                mode: "remote", // required as the combo store shouldn't be loaded before a field name is selected
                //store: this.uniqueValuesStore,
                pageSize: this.autoCompleteCfg.pageSize || this.pageSize,
                typeAhead: false,
                forceSelection: false,
                remoteSort: true,
                triggerAction: "all",
                allowBlank: this.allowBlank,
                displayField: "value",
                valueField: "value",
                minChars: 1,
                resizable: true,
                listeners: {
                    select: function(combo, record) {
                        this.filter.upperBoundary = combo.getValue();
                        this.fireEvent("change", this.filter);
                    },
                    beforequery: function(evt) {
                        evt.combo.store.baseParams.start = 0;
                    },
                    blur: function(combo) {
                        this.filter.upperBoundary = combo.getValue();
                        this.fireEvent("change", this.filter);
                    },
                    scope: this
                },
                width: 50,
                listWidth: 250,
                grow: true,
                growMin: 50,
                anchor: "100%"
            },this.defaultItemsProp['upper'])
        
         
        };
        
        
    },
    
	createInitialValueWidgets: function() {
		var record = this.attributes.getAt(this.attributes.find('name',this.filter.property));
		if(record) {
			this.fieldType = record.get("type").split(":")[1];
			this.createValueWidgets(this.filter.type, [this.filter.lowerBoundary || this.filter.value, this.filter.upperBoundary || this.filter.value], true);
		}
	},
	
    initComponent: function() {
        
        var me = this;
        
        this.combineErrors = false;
        
        if (!this.dateFormat) {
            this.dateFormat = Ext.form.DateField.prototype.format;
        }
        if (!this.timeFormat) {
            this.timeFormat = Ext.form.TimeField.prototype.format;
        }     
		var hasFilter = !!this.filter;
        if(!this.filter) {
            this.filter = this.createDefaultFilter();
        }
        // Maintain compatibility with QueryPanel, which relies on "remote"
        // mode and the filterBy filter applied in it's attributeStore's load
        // listener *after* the initial combo filtering.
        //TODO Assume that the AttributeStore is already loaded and always
        // create a new one without geometry fields.
        var mode = "remote", attributes = new GeoExt.data.AttributeStore();
        if (this.attributes) {
            if (this.attributes.getCount() != 0) {
                mode = "local";
                this.attributes.each(function(r) {
                    var match = /gml:((Multi)?(Point|Line|Polygon|Curve|Surface|Geometry)).*/.exec(r.get("type"));
                    match || attributes.add([r]);
                });
            } else {
                attributes = this.attributes;
            }
        }
		
        this.createDefaultConfigs();
        
        var defAttributesComboConfig = {
            xtype: "combo",
            store: attributes,
            editable: mode == "local",
            typeAhead: true,
            forceSelection: true,
            mode: mode,
            triggerAction: "all",
            ref: "property",
            allowBlank: this.allowBlank,
            displayField: "name",
            valueField: "name",
            value: this.filter.property,
            listeners: {
                select: function(combo, record) {
                    this.filter.property = record.get("name");
                    this.fieldType = record.get("type").split(":")[1];
                    if(!this.comparisonCombo) {
                        this.comparisonCombo = this.items.get(1);
                    }
                    
                    this.comparisonCombo.enable();
                    this.comparisonCombo.reset();
                    
                    if(!this.valueWidgets) {
                        this.valueWidgets = this.items.get(2);
                    }
                    this.valueWidgets.removeAll();
                    
                    this.setFilterType(null);
        
                    this.doLayout();
                    this.fireEvent("change", this.filter, this);
                },
                // workaround for select event not being fired when tab is hit
                // after field was autocompleted with forceSelection
                "blur": function(combo) {
                    var index = combo.store.findExact("name", combo.getValue());
                    if (index != -1) {
                        combo.fireEvent("select", combo, combo.store.getAt(index));
                    } else if (combo.startValue != null) {
                        combo.setValue(combo.startValue);
                    }
                },
                scope: this
            },
            width: 120
        };
        
        var defComparisonComboConfig = {
            xtype: "gxp_comparisoncombo",
            ref: "type",
            disabled: !hasFilter,
            allowBlank: this.allowBlank,
            value: this.filter.type,
            listeners: {
                select: function(combo, record) {                    
                    this.createValueWidgets(record.get("value"), ['','']);
                },
                expand: function(combo) {
                    var store = combo.getStore();
                    store.clearFilter();
                    if(this.fieldType === "date" || this.fieldType === "dateTime" || this.fieldType === "time" || this.fieldType === "int" || this.fieldType === "double" || this.fieldType === "decimal" || this.fieldType === "integer" || this.fieldType === "long" || this.fieldType === "float" || this.fieldType === "short"){
                        store.filter([
                          {
                            fn   : function(record) {
                                return (record.get('name') === "=") || (record.get('name') === "<>") || (record.get('name') === "<") || (record.get('name') === ">") || (record.get('name') === "<=") || (record.get('name') === ">=") || (record.get('name') === "between");
                            },
                            scope: this
                          }                      
                        ]);
                    }/*else if(this.fieldType === "boolean"){
                        store.filter([
                          {
                            fn   : function(record) {
                                return (record.get('name') === "=");
                            },
                            scope: this
                          }                      
                        ]);
                    }*/ 
                },
                scope: this
            }
        };
        
        this.attributesComboConfig = this.attributesComboConfig || {};
        Ext.applyIf(this.attributesComboConfig, defAttributesComboConfig);
        
        this.comparisonComboConfig = this.comparisonComboConfig || {};        
        Ext.applyIf(this.comparisonComboConfig, defComparisonComboConfig);

		
		
		if(hasFilter) {
			if(this.attributes.getCount() === 0) {
				attributes.on("load", function() {
					this.createInitialValueWidgets(attributes);
				}, this);
				attributes.load();
			} else {
				this.on('render', function() {
					this.createInitialValueWidgets(attributes);
				}, this, {single: true});
			}
		}
		
        this.items = [this.attributesComboConfig, this.comparisonComboConfig, {
            xtype: 'container',
            isFormField: true,
            isValid: function() { return true; },
            reset: function() {
                 this.eachItem(function(a) {
                    a.reset()
                });
            },
            eachItem: function(b, a) {
                if (this.items && this.items.each) {
                    this.items.each(b, a || this)
                }
            },
            layout  : 'hbox',            
            defaultMargins: '0 3 0 0',
            width: 100
        }];
		
        
        this.addEvents(
            /**
             * Event: change
             * Fires when the filter changes.
             *
             * Listener arguments:
             * filter - {OpenLayers.Filter} This filter.
             * this - {gxp.form.FilterField} (TODO change sequence of event parameters)
             */
            "change"
        ); 

        gxp.form.FilterField.superclass.initComponent.call(this);
    },

    /**
     * Method: validateValue
     * Performs validation checks on the filter field.
     *
     * Returns:
     * {Boolean} True if value is valid. 
     */
    validateValue: function(value, preventMark) {
        if (this.filter.type === OpenLayers.Filter.Comparison.BETWEEN) {
            return (this.filter.property !== null && this.filter.upperBoundary !== null &&
                this.filter.lowerBoundary !== null);
        } else {
            return (this.filter.property !== null &&
                this.filter.value !== null && this.filter.type !== null);
        }
    },
    
    /**
     * Method: createDefaultFilter
     * May be overridden to change the default filter.
     *
     * Returns:
     * {OpenLayers.Filter} By default, returns a comparison filter.
     */
    createDefaultFilter: function() {
        return new OpenLayers.Filter.Comparison({matchCase: !this.caseInsensitiveMatch});
    },
    
    initUniqueValuesStore: function(store, url, layerName, namespaces, fieldName) {
        var wpsUrl = url;
        if (url.indexOf('wfs?', url.length - 'wfs?'.length) !== -1) {
            wpsUrl = url.substring(0, url.length-'wfs?'.length)+"wps";
        }
            
        var prefix = "";
        var featureTypeName = layerName;
        var featureNS;
        if(layerName.indexOf(':') !== -1) {
            prefix = layerName.split(':')[0];
            featureNS = namespaces[prefix] || '';
        }
            
        var params = {
            url: wpsUrl,
            outputs: [{
                identifier: "result",
                mimeType: "application/json"
            }],               
            inputs: {
                featureTypeName: featureTypeName,
                featureNS: featureNS,
                fieldName: fieldName
            },
            start: 0,
            limit: this.autoCompleteCfg.pageSize || this.pageSize
        };
        store.setWPSParams(params);
    },
    
    setFilterType: function(type) {
        this.filter.type = type;
        
        // Ilike (ignore case)
        if(this.filter.type == "ilike"){
            this.filter.type = OpenLayers.Filter.Comparison.LIKE;
            this.filter.matchCase = false;
        }else{
            // default matches case. See OpenLayers.Filter.Comparison#matchCase
            this.filter.matchCase = true;
        }
    },

    /** api: method[setFilter]
     *  :arg filter: ``OpenLayers.Filter``` Change the filter object to be
     *  used.
     */
    setFilter: function(filter) {
        var previousType = this.filter.type;
        this.filter = filter;
        if (previousType !== filter.type) {
            this.setFilterType(filter.type);
        }
        this['property'].setValue(filter.property);
        this['type'].setValue(filter.type);
        if (filter.type === OpenLayers.Filter.Comparison.BETWEEN) {
            this['lowerBoundary'].setValue(filter.lowerBoundary);
            this['upperBoundary'].setValue(filter.upperBoundary);
        } else {
            this['value'].setValue(filter.value);
        }
        this.fireEvent("change", this.filter, this);
    }

});

Ext.reg('gxp_filterfield', gxp.form.FilterField);
/**
 * Copyright (c) 2008-2011 The Open Planning Project
 * 
 * Published under the GPL license.
 * See https://github.com/opengeo/gxp/raw/master/license.txt for the full text
 * of the license.
 */

/**
 * @include widgets/form/FilterField.js
 */

/** api: (define)
 *  module = gxp
 *  class = FilterBuilder
 *  base_link = `Ext.Panel <http://extjs.com/deploy/dev/docs/?class=Ext.Panel>`_
 */
Ext.namespace("gxp");

/** api: constructor
 *  .. class:: FilterBuilder(config)
 *   
 *      Create a panel for assembling a filter.
 */
gxp.FilterBuilder = Ext.extend(Ext.Container, {

    /** api: config[builderTypeNames]
     *  ``Array``
     *  A list of labels that correspond to builder type constants.
     *  These will be the option names available in the builder type combo.
     *  Default is ``["any", "all", "none", "not all"]``.
     */
    builderTypeNames: ["any", "all", "none", "not all"],
    
    /** api: config[allowedBuilderTypes]
     *  ``Array``
     *  List of builder type constants.  Default is
     *  ``[ANY_OF, ALL_OF, NONE_OF]``.
     */
    allowedBuilderTypes: null,
    
    /** api: config[allowBlank]
     *  ``Boolean`` Do we allow blank FilterFields? It is safe to say true
     *  here, but for compatibility reasons with old applications, the default
     *  is false.
     */
    allowBlank: false,
    
    /** api: config[caseInsensitiveMatch]
     *  ``Boolean``
     *  Should Comparison Filters for Strings do case insensitive matching?  Default is ``"false"``.
     */
    caseInsensitiveMatch: false,

    /** api: config[preComboText]
     *  ``String``
     *  String to display before filter type combo.  Default is ``"Match"``.
     */
    preComboText: "Match",

    /** api: config[postComboText]
     *  ``String``
     *  String to display after filter type combo.  Default is
     *  ``"of the following:"``.
     */
    postComboText: "of the following:",

    /** api: config[cls]
     *  ``String``
     *  The CSS class to be added to this panel's element (defaults to
     *  ``"gxp-filterbuilder"``).
     */
    cls: "gxp-filterbuilder",
    
    /** api: config[filter]
     *  ``OpenLayers.Filter``
     *  Filter to initialize the component with
     */

    /** private: property[builderType]
     */
    builderType: null,

    /** private: property[childFilterContainer]
     */
    childFilterContainer: null,
    
    /** private: property[customizeFilterOnInit]
     */
    customizeFilterOnInit: true,
    
    /** i18n */
    addConditionText: "add condition",
    addGroupText: "add group",
    removeConditionText: "remove condition",

    /** api: config[allowGroups]
     *  ``Boolean``
     *  Allow groups of conditions to be added.  Default is ``true``.
     *  If ``false``, only individual conditions (non-logical filters) can
     *  be added.
     */
    allowGroups: true,
    
    /**
     * Property: attributesComboConfig
     * {Object}
     */
    attributesComboConfig: null,
    
    /** api:config[autoComplete]
     *  ``Boolean`` autocomplete enabled on text fields.
     */
    autoComplete: false,
    
    /** api:config[autoCompleteCfg]
     *  ``Object`` autocomplete configuration object.
     */
    autoCompleteCfg: {},

    initComponent: function() {
        var defConfig = {
            defaultBuilderType: gxp.FilterBuilder.ANY_OF
        };
        Ext.applyIf(this, defConfig);
        
        if(this.customizeFilterOnInit) {
            this.filter = this.customizeFilter(this.filter);
        }
        
        this.builderType = this.getBuilderType();
        
        this.items = [{
            xtype: "container",
            layout: "form",
            ref: "form",
            defaults: {anchor: "100%"},
            hideLabels: true,
            items: [{
                xtype: "compositefield",
                style: "padding-left: 2px",
                items: [{
                    xtype: "label",
                    style: "padding-top: 0.3em",
                    text: this.preComboText
                }, this.createBuilderTypeCombo(), {
                    xtype: "label",
                    style: "padding-top: 0.3em",
                    text: this.postComboText
                }]
            }, this.createChildFiltersPanel(), {
                xtype: "toolbar",
                items: this.createToolBar()
            }]
        
        }];
        
        this.addEvents(
            /**
             * Event: change
             * Fires when the filter changes.
             *
             * Listener arguments:
             * builder - {gxp.FilterBuilder} This filter builder.  Call
             *     ``getFilter`` to get the updated filter.
             */
            "change"
        ); 

        gxp.FilterBuilder.superclass.initComponent.call(this);
    },

    /** private: method[createToolBar]
     */
    createToolBar: function() {
        var bar = [{
            text: this.addConditionText,
            iconCls: "add",
            handler: function() {
                this.addCondition();
            },
            scope: this
        }];
        if(this.allowGroups) {
            bar.push({
                text: this.addGroupText,
                iconCls: "add",
                handler: function() {
                    this.addCondition(true);
                },
                scope: this
            });
        }
        return bar;
    },
    
    /** api: method[getFilter]
     *  :return: ``OpenLayers.Filter``
     *  
     *  Returns a filter that fits the model in the Filter Encoding
     *  specification.  Use this method instead of directly accessing
     *  the ``filter`` property.  Return will be ``false`` if any child
     *  filter does not have a type, property, or value.
     */
    getFilter: function() {
        var filter;
        if(this.filter) {
            filter = this.filter.clone();
            if(filter instanceof OpenLayers.Filter.Logical) {
                filter = this.cleanFilter(filter);
            }
        }
        return filter;
    },
    
    /** private: method[cleanFilter]
     *  :arg filter: ``OpenLayers.Filter.Logical``
     *  :return: ``OpenLayers.Filter`` An equivalent filter to the input, where
     *      all binary logical filters have more than one child filter.  Returns
     *      false if a filter doesn't have non-null type, property, or value.
     *  
     *  Ensures that binary logical filters have more than one child.
     */
    cleanFilter: function(filter) {
        if(filter instanceof OpenLayers.Filter.Logical) {
            if(filter.type !== OpenLayers.Filter.Logical.NOT &&
               filter.filters.length === 1) {
                filter = this.cleanFilter(filter.filters[0]);
            } else {
                var child;
                for(var i=0, len=filter.filters.length; i<len; ++i) {
                    child = filter.filters[i];
                    if(child instanceof OpenLayers.Filter.Logical) {
                        child = this.cleanFilter(child);
                        if(child) {
                            filter.filters[i] = child;
                        } else {
                            filter = child;
                            break;
                        }
                    //} else if(!child || child.type === null || child.property === null || child[filter.type === OpenLayers.Filter.Comparison.BETWEEN ? "lowerBoundary" : "value"] === null || child[filter.type === OpenLayers.Filter.Comparison.BETWEEN ? "upperBoundary" : "value"] === null) {
                    } else if(!child || child.type === null || child[child.property] === null || child[child.type === OpenLayers.Filter.Comparison.BETWEEN ? "lowerBoundary" : "value"] === null || child[child.type === OpenLayers.Filter.Comparison.BETWEEN ? "upperBoundary" : "value"] === null ) {
                        filter = false;
                        break;
                    }
                }
            }
        } else {
            if(!filter || filter.type === null || filter.property === null || filter[filter.type === OpenLayers.Filter.Comparison.BETWEEN ? "lowerBoundary" : "value"] === null || filter[filter.type === OpenLayers.Filter.Comparison.BETWEEN ? "upperBoundary" : "value"] === null) {
                filter = false;
            }
        }
        return filter;
    },
    
    /** private: method[customizeFilter]
     *  :arg filter: ``OpenLayers.Filter``  This filter will not
     *      be modified.  Register for events to receive an updated filter, or
     *      call ``getFilter``.
     *  :return: ``OpenLayers.Filter``  A filter that fits the model used by
     *      this builder.
     *  
     *  Create a filter that fits the model for this filter builder.  This filter
     *  will not necessarily meet the Filter Encoding specification.  In
     *  particular, filters representing binary logical operators may not
     *  have two child filters.  Use the <getFilter> method to return a
     *  filter that meets the encoding spec.
     */
    customizeFilter: function(filter) {
        if(!filter) {
            filter = this.wrapFilter(this.createDefaultFilter());
        } else {
            filter = this.cleanFilter(filter);
            var child, i, len;
            switch(filter.type) {
                case OpenLayers.Filter.Logical.AND:
                case OpenLayers.Filter.Logical.OR:
                    if(!filter.filters || filter.filters.length === 0) {
                        // give the filter children if it has none
                        filter.filters = [this.createDefaultFilter()];
                    } else {
                        for(i=0, len=filter.filters.length; i<len; ++i) {
                            child = filter.filters[i];
                            if(child instanceof OpenLayers.Filter.Logical) {
                                filter.filters[i] = this.customizeFilter(child);
                            }
                        }
                    }
                    // wrap in a logical OR
                    filter = new OpenLayers.Filter.Logical({
                        type: OpenLayers.Filter.Logical.OR,
                        filters: [filter]
                    });
                    break;
                case OpenLayers.Filter.Logical.NOT:
                    if(!filter.filters || filter.filters.length === 0) {
                        filter.filters = [
                            new OpenLayers.Filter.Logical({
                                type: OpenLayers.Filter.Logical.OR,
                                filters: [this.createDefaultFilter()]
                            })
                        ];
                    } else {
                        // NOT filters should have one child only
                        child = filter.filters[0];
                        if(child instanceof OpenLayers.Filter.Logical) {
                            if(child.type !== OpenLayers.Filter.Logical.NOT) {
                                // check children of AND and OR
                                var grandchild;
                                for(i=0, len=child.filters.length; i<len; ++i) {
                                    grandchild = child.filters[i];
                                    if(grandchild instanceof OpenLayers.Filter.Logical) {
                                        child.filters[i] = this.customizeFilter(grandchild);
                                    }
                                }
                            } else {
                                // silly double negative
                                if(child.filters && child.filters.length > 0) {
                                    filter = this.customizeFilter(child.filters[0]);
                                } else {
                                    filter = this.wrapFilter(this.createDefaultFilter());
                                }
                            }
                        } else {
                            // non-logical child of NOT should be wrapped
                            var type;
                            if(this.defaultBuilderType === gxp.FilterBuilder.NOT_ALL_OF) {
                                type = OpenLayers.Filter.Logical.AND;
                            } else {
                                type = OpenLayers.Filter.Logical.OR;
                            }
                            filter.filters = [
                                new OpenLayers.Filter.Logical({
                                    type: type,
                                    filters: [child]
                                })
                            ];
                        }
                    }
                    break;
                default:
                    // non-logical filters get wrapped
                    filter = this.wrapFilter(filter);
                    break;
            }
        }
        return filter;
    },

    createDefaultFilter: function() {
        return new OpenLayers.Filter.Comparison({
                            matchCase: !this.caseInsensitiveMatch});
    },
    
    /** private: method[wrapFilter]
     *  :arg filter: ``OpenLayers.Filter`` A non-logical filter.
     *  :return: ``OpenLayers.Filter`` A wrapped version of the input filter.
     *  
     *  Given a non-logical filter, this creates parent filters depending on
     *  the ``defaultBuilderType``.
     */
    wrapFilter: function(filter) {
        var type;
        if(this.defaultBuilderType === gxp.FilterBuilder.ALL_OF) {
            type = OpenLayers.Filter.Logical.AND;
        } else {
            type = OpenLayers.Filter.Logical.OR;
        }
        return new OpenLayers.Filter.Logical({
            type: OpenLayers.Filter.Logical.OR,
            filters: [
                new OpenLayers.Filter.Logical({
                    type: type, filters: [filter]
                })
            ]
        });
    },
    
    /** private: method[addCondition]
     *  Add a new condition or group of conditions to the builder.  This
     *  modifies the filter and adds a panel representing the new condition
     *  or group of conditions.
     */
    addCondition: function(group) {
        var filter, type;
        if(group) {
            type = "gxp_filterbuilder";
            filter = this.wrapFilter(this.createDefaultFilter());
        } else {
            type = "gxp_filterfield";
            filter = this.createDefaultFilter();
        }
        var newChild = this.newRow({
            xtype: type,
            filter: filter,
            columnWidth: 1,
            attributes: this.attributes,
            validators: this.validators,
            autoComplete: this.autoComplete,
            autoCompleteCfg: this.autoCompleteCfg,
            allowBlank: group ? undefined : this.allowBlank,
            customizeFilterOnInit: group && false,
            caseInsensitiveMatch: this.caseInsensitiveMatch,
            listeners: {
                change: function() {
                    this.fireEvent("change", this);
                },
                scope: this
            }
        });
        this.childFilterContainer.add(newChild);
        this.filter.filters[0].filters.push(filter);
        this.childFilterContainer.doLayout();
    },
    
    /** private: method[removeCondition]
     *  Remove a condition or group of conditions from the builder.  This
     *  modifies the filter and removes the panel representing the condition
     *  or group of conditions.
     */
    removeCondition: function(item, filter) {
        var parent = this.filter.filters[0].filters;
        if(parent.length > 0) {
            parent.remove(filter);
            this.childFilterContainer.remove(item, true);
        }
        if(parent.length === 0) {
            this.addCondition();
        }
        this.fireEvent("change", this);
    },
    
    createBuilderTypeCombo: function() {
        var types = this.allowedBuilderTypes || [
            gxp.FilterBuilder.ANY_OF, gxp.FilterBuilder.ALL_OF,
            gxp.FilterBuilder.NONE_OF
        ];
        var numTypes = types.length;
        var data = new Array(numTypes);
        var type;
        for(var i=0; i<numTypes; ++i) {
            type = types[i];
            data[i] = [type, this.builderTypeNames[type]];
        }
        return {
            xtype: "combo",
            store: new Ext.data.SimpleStore({
                data: data,
                fields: ["value", "name"]
            }),
            value: this.builderType,
            ref: "../../builderTypeCombo",
            displayField: "name",
            valueField: "value",
            triggerAction: "all",
            mode: "local",
            listeners: {
                select: function(combo, record) {
                    this.changeBuilderType(record.get("value"));
                    this.fireEvent("change", this);
                },
                scope: this
            },
            width: 60 // TODO: move to css
        };
    },
    
    /** private: method[changeBuilderType]
     *  :arg type: ``Integer`` One of the filter type constants.
     *  
     *  Alter the filter types when the filter type combo changes.
     */
    changeBuilderType: function(type) {
        if(type !== this.builderType) {
            this.builderType = type;
            var child = this.filter.filters[0];
            switch(type) {
                case gxp.FilterBuilder.ANY_OF:
                    this.filter.type = OpenLayers.Filter.Logical.OR;
                    child.type = OpenLayers.Filter.Logical.OR;
                    break;
                case gxp.FilterBuilder.ALL_OF:
                    this.filter.type = OpenLayers.Filter.Logical.OR;
                    child.type = OpenLayers.Filter.Logical.AND;
                    break;
                case gxp.FilterBuilder.NONE_OF:
                    this.filter.type = OpenLayers.Filter.Logical.NOT;
                    child.type = OpenLayers.Filter.Logical.OR;
                    break;
                case gxp.FilterBuilder.NOT_ALL_OF:
                    this.filter.type = OpenLayers.Filter.Logical.NOT;
                    child.type = OpenLayers.Filter.Logical.AND;
                    break;
            }
        }
    },

    /** private: method[createChildFiltersPanel]
     *  :return: ``Ext.Container``
     *  
     *  Create the panel that holds all conditions and condition groups.  Since
     *  this is called after this filter has been customized, we always
     *  have a logical filter with one child filter - that child is also
     *  a logical filter.
     */
    createChildFiltersPanel: function() {
        this.childFilterContainer = new Ext.Container();
        var grandchildren = this.filter.filters[0].filters;
        var grandchild;
        for(var i=0, len=grandchildren.length; i<len; ++i) {
            grandchild = grandchildren[i];
            var fieldCfg = {
                xtype: "gxp_filterfield",
                allowBlank: this.allowBlank,
                columnWidth: 1,
                filter: grandchild,
                attributes: this.attributes,
                validators: this.validators,
                autoComplete: this.autoComplete,
                autoCompleteCfg: this.autoCompleteCfg,
                listeners: {
                    change: function() {
                        this.fireEvent("change", this);
                    },
                    scope: this
                }
            };
            var containerCfg = Ext.applyIf(
                grandchild instanceof OpenLayers.Filter.Logical ?
                    {
                        xtype: "gxp_filterbuilder"
                    } : {
                        xtype: "container",
                        layout: "form",
                        hideLabels: true,
                        items: fieldCfg
                    }, fieldCfg);
                
            this.childFilterContainer.add(this.newRow(containerCfg));
        }
        return this.childFilterContainer;
    },

    /** private: method[newRow]
     *  :return: ``Ext.Container`` A container that serves as a row in a child
     *  filters panel.
     *  
     *  Generate a "row" for the child filters panel.  This couples another
     *  filter panel or filter builder with a component that allows for
     *  condition removal.
     */
    newRow: function(filterContainer) {
        var ct = new Ext.Container({
            layout: "column",
            items: [{
                xtype: "container",
                width: 28,
                height: 26,
                style: "padding-left: 2px",
                items: {
                    xtype: "button",
                    tooltip: this.removeConditionText,
                    iconCls: "delete",
                    handler: function(btn){
                        this.removeCondition(ct, filterContainer.filter);
                    },
                    scope: this
                }
            }, filterContainer]
        });
        return ct;
    },

    /** private: method[getBuilderType]
     *
     *  :return: ``Integer``  One of the builder type constants.
     *  Determine the builder type based on this filter.
     */
    getBuilderType: function() {
        var type = this.defaultBuilderType;
        if(this.filter) {
            var child = this.filter.filters[0];
            if(this.filter.type === OpenLayers.Filter.Logical.NOT) {
                switch(child.type) {
                    case OpenLayers.Filter.Logical.OR:
                        type = gxp.FilterBuilder.NONE_OF;
                        break;
                    case OpenLayers.Filter.Logical.AND:
                        type = gxp.FilterBuilder.NOT_ALL_OF;
                        break;
                }
            } else {
                switch(child.type) {
                    case OpenLayers.Filter.Logical.OR:
                        type = gxp.FilterBuilder.ANY_OF;
                        break;
                    case OpenLayers.Filter.Logical.AND:
                        type = gxp.FilterBuilder.ALL_OF;
                        break;
                }
            }
        }
        return type;
    },

    /** api: method[setFilter]
     *
     *  :param filter: ``OpenLayers.Filter``
     *
     *  Change the filter associated with this instance.
     */
    setFilter: function(filter) {
        this.filter = this.customizeFilter(filter);
        this.changeBuilderType(this.getBuilderType());
        this.builderTypeCombo.setValue(this.builderType);
        this.form.remove(this.childFilterContainer);
        this.form.insert(1, this.createChildFiltersPanel());
        this.form.doLayout();
        this.fireEvent("change", this);
    }

});

/**
 * Builder Types
 */
gxp.FilterBuilder.ANY_OF = 0;
gxp.FilterBuilder.ALL_OF = 1;
gxp.FilterBuilder.NONE_OF = 2;
gxp.FilterBuilder.NOT_ALL_OF = 3;

/** api: xtype = gxp_filterbuilder */
Ext.reg('gxp_filterbuilder', gxp.FilterBuilder); 
/**
* Copyright (c) 2008-2011 The Open Planning Project
*
* Published under the GPL license.
* See https://github.com/opengeo/gxp/raw/master/license.txt for the full text
* of the license.
*/

/**
 * @include widgets/ScaleLimitPanel.js
 * @include widgets/TextSymbolizer.js
 * @include widgets/PolygonSymbolizer.js
 * @include widgets/LineSymbolizer.js
 * @include widgets/PointSymbolizer.js
 * @include widgets/FilterBuilder.js
 */

/** api: (define)
 *  module = gxp
 *  class = RulePanel
 *  base_link = `Ext.TabPanel <http://extjs.com/deploy/dev/docs/?class=Ext.TabPanel>`_
 */
Ext.namespace("gxp");

/** api: constructor
 *  .. class:: RulePanel(config)
 *   
 *      Create a panel for assembling SLD rules.
 */
gxp.RulePanel = Ext.extend(Ext.TabPanel, {
    
    /** api: property[fonts]
     *  ``Array(String)`` List of fonts for the font combo.  If not set,
     *      defaults  to the list provided by the <Styler.FontComboBox>.
     */
    fonts: undefined,

    /** api: property[symbolType]
     *  ``String`` One of "Point", "Line", or "Polygon".  If no rule is 
     *  provided, default is "Point".
     */
    symbolType: "Point",

    /** api: config[rule]
     *  ``OpenLayers.Rule`` Optional rule provided in the initial
     *  configuration.  If a rule is provided and no `symbolType` is provided,
     *  the symbol type will be derived from the first symbolizer found in the
     *  rule.
     */
    rule: null,
    
    /** private: property[attributes]
     *  ``GeoExt.data.AttributeStore`` A configured attributes store for use
     *  in the filter property combo.
     */
    attributes: null,
    
    /** private: property[pointGraphics]
     *  ``Array`` A list of objects to be used as the root of the data for a
     *  JsonStore.  These will become records used in the selection of
     *  a point graphic.  If an object in the list has no "value" property,
     *  the user will be presented with an input to provide their own URL
     *  for an external graphic.  By default, names of well-known marks are
     *  provided.  In addition, the default list will produce a record with
     *  display of "external" that create an input for an external graphic
     *  URL.
     *
     *  Fields:
     * 
     *  * display - ``String`` The name to be displayed to the user.
     *  * preview - ``String`` URL to a graphic for preview.
     *  * value - ``String`` Value to be sent to the server.
     *  * mark - ``Boolean`` The value is a well-known name for a mark.  If
     *    false, the value will be assumed to be a url for an external graphic.
     */

    /** private: property[nestedFilters]
     *  ``Boolean`` Allow addition of nested logical filters.  This sets the
     *  allowGroups property of the filter builder.  Default is true.
     */
    nestedFilters: true,
    
    /** private: property[minScaleDenominatorLimit]
     *  ``Number`` Lower limit for scale denominators.  Default is what you get
     *  when  you assume 20 zoom levels starting with the world in Spherical
     *  Mercator on a single 256 x 256 tile at zoom 0 where the zoom factor is
     *  2.
     */
    minScaleDenominatorLimit: Math.pow(0.5, 19) * 40075016.68 * 39.3701 * OpenLayers.DOTS_PER_INCH / 256,

    /** private: property[maxScaleDenominatorLimit]
     *  ``Number`` Upper limit for scale denominators.  Default is what you get
     *  when you project the world in Spherical Mercator onto a single
     *  256 x 256 pixel tile and assume OpenLayers.DOTS_PER_INCH (this
     *  corresponds to zoom level 0 in Google Maps).
     */
    maxScaleDenominatorLimit: 40075016.68 * 39.3701 * OpenLayers.DOTS_PER_INCH / 256,
    
    /** private: property [scaleLevels]
     *  ``Number`` Number of scale levels to assume.  This is only for scaling
     *  values exponentially along the slider.  Scale values are not
     *  required to one of the discrete levels.  Default is 20.
     */
    scaleLevels: 20,
    
    /** private: property[scaleSliderTemplate]
     *  ``String`` Template for the tip displayed by the scale threshold slider.
     *
     *  Can be customized using the following keywords in curly braces:
     *  
     *  * zoom - the zoom level
     *  * scale - the scale denominator
     *  * type - "Max" or "Min" denominator
     *  * scaleType - "Min" or "Max" scale (sense is opposite of type)
     *
     *  Default is "{scaleType} Scale 1:{scale}".
     */
    scaleSliderTemplate: "{scaleType} Scale 1:{scale}",
    
    /** private: method[modifyScaleTipContext]
     *  Called from the multi-slider tip's getText function.  The function
     *  will receive two arguments - a reference to the panel and a data
     *  object.  The data object will have scale, zoom, and type properties
     *  already calculated.  Other properties added to the data object
     *  are available to the <scaleSliderTemplate>.
     */
    modifyScaleTipContext: Ext.emptyFn,
    
    /** i18n */
    labelFeaturesText: "Label Features",
    labelsText: "Labels",
    basicText: "Basic",
    advancedText: "Advanced",
    limitByScaleText: "Limit by scale",
    limitByConditionText: "Limit by condition",
    symbolText: "Symbol",
    nameText: "Name",

    /** private */
    initComponent: function() {
        
        var defConfig = {
            plain: true,
            border: false
        };
        Ext.applyIf(this, defConfig);
        
        if(!this.rule) {
            this.rule = new OpenLayers.Rule({
                name: this.uniqueRuleName()
            });
        } else {
            if (!this.initialConfig.symbolType) {
                this.symbolType = this.getSymbolTypeFromRule(this.rule) || this.symbolType;
            }
        }
        
        this.activeTab = 0;
        
        this.textSymbolizer = new gxp.TextSymbolizer({
            symbolizer: this.getTextSymbolizer(),
            attributes: this.attributes,
            fonts: this.fonts,
            listeners: {
                change: function(symbolizer) {
                    this.fireEvent("change", this, this.rule);
                },
                scope: this
            }
        });
        
        /**
         * The interpretation here is that scale values of zero are equivalent to
         * no scale value.  If someone thinks that a scale value of zero should have
         * a different interpretation, this needs to be changed.
         */
        this.scaleLimitPanel = new gxp.ScaleLimitPanel({
            maxScaleDenominator: this.rule.maxScaleDenominator || undefined,
            limitMaxScaleDenominator: !!this.rule.maxScaleDenominator,
            maxScaleDenominatorLimit: this.maxScaleDenominatorLimit,
            minScaleDenominator: this.rule.minScaleDenominator || undefined,
            limitMinScaleDenominator: !!this.rule.minScaleDenominator,
            minScaleDenominatorLimit: this.minScaleDenominatorLimit,
            scaleLevels: this.scaleLevels,
            scaleSliderTemplate: this.scaleSliderTemplate,
            modifyScaleTipContext: this.modifyScaleTipContext,
            listeners: {
                change: function(comp, min, max) {
                    this.rule.minScaleDenominator = min;
                    this.rule.maxScaleDenominator = max;
                    this.fireEvent("change", this, this.rule);
                },
                scope: this
            }
        });
        
        this.filterBuilder = new gxp.FilterBuilder({
            allowGroups: this.nestedFilters,
            filter: this.rule && this.rule.filter && this.rule.filter.clone(),
            attributes: this.attributes,
            listeners: {
                change: function(builder) {
                    var filter = builder.getFilter(); 
                    this.rule.filter = filter;
                    this.fireEvent("change", this, this.rule);
                },
                scope: this
            }
        });
        
        this.items = [{
            title: this.labelsText,
            autoScroll: true,
            bodyStyle: {"padding": "10px"},
            items: [{
                xtype: "fieldset",
                title: this.labelFeaturesText,
                autoHeight: true,
                checkboxToggle: true,
                collapsed: !this.hasTextSymbolizer(),
                items: [
                    this.textSymbolizer
                ],
                listeners: {
                    collapse: function() {
                        OpenLayers.Util.removeItem(this.rule.symbolizers, this.getTextSymbolizer());
                        this.fireEvent("change", this, this.rule);
                    },
                    expand: function() {
                        this.setTextSymbolizer(this.textSymbolizer.symbolizer);
                        this.fireEvent("change", this, this.rule);
                    },
                    scope: this
                }
            }]
        }];
        if (this.getSymbolTypeFromRule(this.rule) || this.symbolType) {
            this.items = [{
                title: this.basicText,
                autoScroll: true,
                items: [this.createHeaderPanel(), this.createSymbolizerPanel()]
            }, this.items[0], {
                title: this.advancedText,
                defaults: {
                    style: {
                        margin: "7px"
                    }
                },
                autoScroll: true,
                items: [{
                    xtype: "fieldset",
                    title: this.limitByScaleText,
                    checkboxToggle: true,
                    collapsed: !(this.rule && (this.rule.minScaleDenominator || this.rule.maxScaleDenominator)),
                    autoHeight: true,
                    items: [this.scaleLimitPanel],
                    listeners: {
                        collapse: function() {
                            delete this.rule.minScaleDenominator;
                            delete this.rule.maxScaleDenominator;
                            this.fireEvent("change", this, this.rule);
                        },
                        expand: function() {
                            /**
                             * Start workaround for
                             * http://projects.opengeo.org/suite/ticket/676
                             */
                            var tab = this.getActiveTab();
                            this.activeTab = null;
                            this.setActiveTab(tab);
                            /**
                             * End workaround for
                             * http://projects.opengeo.org/suite/ticket/676
                             */
                            var changed = false;
                            if (this.scaleLimitPanel.limitMinScaleDenominator) {
                                this.rule.minScaleDenominator = this.scaleLimitPanel.minScaleDenominator;
                                changed = true;
                            }
                            if (this.scaleLimitPanel.limitMaxScaleDenominator) {
                                this.rule.maxScaleDenominator = this.scaleLimitPanel.maxScaleDenominator;
                                changed = true;
                            }
                            if (changed) {
                                this.fireEvent("change", this, this.rule);
                            }
                        },
                        scope: this
                    }
                }, {
                    xtype: "fieldset",
                    title: this.limitByConditionText,
                    checkboxToggle: true,
                    collapsed: !(this.rule && this.rule.filter),
                    autoHeight: true,
                    items: [this.filterBuilder],
                    listeners: {
                        collapse: function(){
                            delete this.rule.filter;
                            this.fireEvent("change", this, this.rule);
                        },
                        expand: function(){
                            var changed = false;
                            this.rule.filter = this.filterBuilder.getFilter();
                            this.fireEvent("change", this, this.rule);
                        },
                        scope: this
                    }
                }]
            }];
        };
        this.items[0].autoHeight = true;

        this.addEvents(
            /** api: events[change]
             *  Fires when any rule property changes.
             *
             *  Listener arguments:
             *  * panel - :class:`gxp.RulePanel` This panel.
             *  * rule - ``OpenLayers.Rule`` The updated rule.
             */
            "change"
        ); 
        
        this.on({
            tabchange: function(panel, tab) {
                tab.doLayout();
            },
            scope: this
        });

        gxp.RulePanel.superclass.initComponent.call(this);
    },

    /** private: method[hasTextSymbolizer]
     */
    hasTextSymbolizer: function() {
        var candidate, symbolizer;
        for (var i=0, ii=this.rule.symbolizers.length; i<ii; ++i) {
            candidate = this.rule.symbolizers[i];
            if (candidate instanceof OpenLayers.Symbolizer.Text) {
                symbolizer = candidate;
                break;
            }
        }
        return symbolizer;
    },
    
    /** private: method[getTextSymbolizer]
     *  Get the first text symbolizer in the rule.  If one does not exist,
     *  create one.
     */
    getTextSymbolizer: function() {
        var symbolizer = this.hasTextSymbolizer();
        if (!symbolizer) {
            symbolizer = new OpenLayers.Symbolizer.Text();
        }
        return symbolizer;
    },
    
    /** private: method[setTextSymbolizer]
     *  Update the first text symbolizer in the rule.  If one does not exist,
     *  add it.
     */
    setTextSymbolizer: function(symbolizer) {
        var found;
        for (var i=0, ii=this.rule.symbolizers.length; i<ii; ++i) {
            candidate = this.rule.symbolizers[i];
            if (this.rule.symbolizers[i] instanceof OpenLayers.Symbolizer.Text) {
                this.rule.symbolizers[i] = symbolizer;
                found = true;
                break;
            }
        }
        if (!found) {
            this.rule.symbolizers.push(symbolizer);
        }        
    },

    /** private: method[uniqueRuleName]
     *  Generate a unique rule name.  This name will only be unique for this
     *  session assuming other names are created by the same method.  If
     *  name needs to be unique given some other context, override it.
     */
    uniqueRuleName: function() {
        return OpenLayers.Util.createUniqueID("rule_");
    },
    
    /** private: method[createHeaderPanel]
     *  Creates a panel config containing rule name, symbolizer, and scale
     *  constraints.
     */
    createHeaderPanel: function() {
        this.symbolizerSwatch = new GeoExt.FeatureRenderer({
            symbolType: this.symbolType,
            isFormField: true,
            fieldLabel: this.symbolText
        });
        return {
            xtype: "form",
            border: false,
            labelAlign: "top",
            defaults: {border: false},
            style: {"padding": "0.3em 0 0 1em"},
            items: [{
                layout: "column",
                defaults: {
                    border: false,
                    style: {"padding-right": "1em"}
                },
                items: [{
                    layout: "form",
                    width: 150,
                    items: [{
                        xtype: "textfield",
                        fieldLabel: this.nameText,
                        anchor: "95%",
                        value: this.rule && (this.rule.title || this.rule.name || ""),
                        listeners: {
                            change: function(el, value) {
                                this.rule.title = value;
                                this.fireEvent("change", this, this.rule);
                            },
                            scope: this
                        }
                    }]
                }, {
                    layout: "form",
                    width: 70,
                    items: [this.symbolizerSwatch]
                }]
            }]
        };
    },

    /** private: method[createSymbolizerPanel]
     */
    createSymbolizerPanel: function() {
        // use first symbolizer that matches symbolType
        var candidate, symbolizer;
        var Type = OpenLayers.Symbolizer[this.symbolType];
        var existing = false;
        if (Type) {
            for (var i=0, ii=this.rule.symbolizers.length; i<ii; ++i) {
                candidate = this.rule.symbolizers[i];
                if (candidate instanceof Type) {
                    existing = true;
                    symbolizer = candidate;
                    break;
                }
            }
            if (!symbolizer) {
                // allow addition of new symbolizer
                symbolizer = new Type({fill: false, stroke: false});
            }
        } else {
            throw new Error("Appropriate symbolizer type not included in build: " + this.symbolType);
        }
        this.symbolizerSwatch.setSymbolizers([symbolizer],
            {draw: this.symbolizerSwatch.rendered}
        );
        var cfg = {
            xtype: "gxp_" + this.symbolType.toLowerCase() + "symbolizer",
            symbolizer: symbolizer,
            bodyStyle: {padding: "10px"},
            border: false,
            labelWidth: 70,
            defaults: {
                labelWidth: 70
            },
            listeners: {
                change: function(symbolizer) {
                    this.symbolizerSwatch.setSymbolizers(
                        [symbolizer], {draw: this.symbolizerSwatch.rendered}
                    );
                    if (!existing) {
                        this.rule.symbolizers.push(symbolizer);
                        existing = true;
                    }
                    this.fireEvent("change", this, this.rule);
                },
                scope: this
            }
        };
        if (this.symbolType === "Point" && this.pointGraphics) {
            cfg.pointGraphics = this.pointGraphics;
        }
        return cfg;
        
    },

    /** private: method[getSymbolTypeFromRule]
     *  :arg rule: `OpenLayers.Rule`
     *  :return: `String` "Point", "Line" or "Polygon" (or undefined if none
     *      of the three.
     *
     *  Determines the symbol type of the first symbolizer of a rule that is
     *  not a text symbolizer
     */
    getSymbolTypeFromRule: function(rule) {
        var candidate, type;
        for (var i=0, ii=rule.symbolizers.length; i<ii; ++i) {
            candidate = rule.symbolizers[i];
            if (!(candidate instanceof OpenLayers.Symbolizer.Text)) {
                type = candidate.CLASS_NAME.split(".").pop();
                break;
            }
        }
        return type;
    }

});

/** api: xtype = gxp_rulepanel */
Ext.reg('gxp_rulepanel', gxp.RulePanel); 
