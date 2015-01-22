var ESPStyler = (function() {

	var SLD;
	var layerName;
	var symbolType;
	var attributesInfo;

	var panel;
	
	var rulesFieldSet;
	var rulesToolbar;
	
	var selectedRule;
	var userStyle;
	
	var legend;

	function createRule() {
        return new OpenLayers.Rule({
            symbolizers: [new OpenLayers.Symbolizer[symbolType]]
        });
    }
	
	function addRule() {
        userStyle.rules.push(createRule());
        updateStyle();
	}
	
	function saveRule(rule, doNotNotify) {
        var i = userStyle.rules.indexOf(selectedRule);
        userStyle.rules[i] = rule;
	
		updateStyle(doNotNotify);
	}
	
	function updateStyle(doNotNotify) {
		var format = new OpenLayers.Format.SLD({multipleSymbolizers: true});
		var newStyle = format.write({
           namedLayers: [{
               name: layerName,
               userStyles: [userStyle]
           }]
        });
		legend.destroy();
		legend = createLegend(userStyle.rules, {symbolType: symbolType, selectable: true});
	
		Ext.getDom('advancedStylerConfig').value = newStyle;
		if(!doNotNotify) {
			sendEvent('change', Ext.getDom('advancedStylerConfig'));
		}
	}
	
	function sendEvent(eventName, element) {
		var event;

		if (document.createEvent) {
			event = document.createEvent("HTMLEvents");
			event.initEvent(eventName, true, true);
		} else {
			event = document.createEventObject();
			event.eventType = eventName;
		}

		  event.eventName = eventName;

		if (document.createEvent) {
			element.dispatchEvent(event);
		} else {
			element.fireEvent("on" + event.eventType, event);
		}
	}
	
	function editRule() {
		var rule = selectedRule;
        var origRule = rule.clone();

        var ruleDlg = new Ext.Window({
            title: String.format("Rules",
                rule.title || rule.name || "New Rule"),
            width: 340,
            autoHeight: true,
            modal: true,
            items: [{
                xtype: "gxp_rulepanel",
                ref: "rulePanel",
                symbolType: symbolType,
                rule: rule,
                attributes: new GeoExt.data.AttributeStore({
					data: attributesInfo
                    /*url: "http://localhost:8080/geoserver/ows",
                    baseParams: {
                        "SERVICE": "WFS",
                        "REQUEST": "DescribeFeatureType",
                        "TYPENAME": layerName
                    },
                    method: "GET",
                    disableCaching: false*/
                }),
                border: false,
                defaults: {
                    autoHeight: true,
                    hideMode: "offsets"
                },
                listeners: {
                    "change": function() {},
                    "tabchange": function() {ruleDlg.syncShadow();}
                }
            }],
            bbar: ["->", {
                text: "Cancel",
                iconCls: "cancel",
                handler: function() {
                    saveRule(origRule, true);
                    ruleDlg.close();
                }
            }, {
                text: "Save",
                iconCls: "save",
                handler: function() { 
					saveRule(rule);
					ruleDlg.close(); 
				}
            }]
        });
        ruleDlg.show();
	}
	
	function removeRule() {
		userStyle.rules.remove(selectedRule);
		legend.selectedRule = null;
        updateStyle();
	}

	function duplicateRule() {
        var newRule = selectedRule.clone();
        userStyle.rules.push(newRule);
        updateStyle();
        updateRuleRemoveButton();
	}
	
	function updateRuleRemoveButton() {
        rulesToolbar.items.get(1).setDisabled(!selectedRule);
    }
	
	function doLayout() {
		panel.doLayout();
	}
	
	function parseStyle(selectable) {
        var data = new OpenLayers.Format.XML().read(SLD);
        
        var format = new OpenLayers.Format.SLD({multipleSymbolizers: true});
        
        try {
            var sld = format.read(data);
            var userStyles = sld.namedLayers[layerName].userStyles;

            for (var i=0, len=userStyles.length; i<len; ++i) {
                userStyle = userStyles[i];
            }
			for (var j=0, rulesLen=userStyle.rules.length; j<rulesLen; j++) {
				var rule = userStyle.rules[j];
				for (var k=0, symbLen=rule.symbolizers.length; k<symbLen; k++) {
					normalizeSymbolizer(rule.symbolizers[k]);
				}
			}
			if(userStyle) {
				legend = createLegend(userStyle.rules, {symbolType: symbolType, selectable:selectable});
			}
        }
        catch(e) {
            alert('error');
        }
    }
	
	function normalizeSymbolizer(symbolizer) {
		if(symbolizer instanceof OpenLayers.Symbolizer.Point) {
			if(!symbolizer.pointRadius) {
				symbolizer.pointRadius=5;
			}
		}
	}

	function createLegend(rules, options) {
        options = Ext.applyIf(options || {}, {enableDD: true});
        if(options.symbolType) {
			symbolType = options.symbolType;
		}
        if (!symbolType) {
            var typeHierarchy = ["Point", "Line", "Polygon"];
            // use the highest symbolizer type of the 1st rule
            highest = 0;
            var symbolizers = rules[0].symbolizers, symbolType;
            for (var i=symbolizers.length-1; i>=0; i--) {
                symbolType = symbolizers[i].CLASS_NAME.split(".").pop();
                highest = Math.max(highest, typeHierarchy.indexOf(symbolType));
            }
            symbolType = typeHierarchy[highest];
        }
        var legend = rulesFieldSet.add({
            xtype: "gx_vectorlegend",
            showTitle: false,
            height: rules.length > 10 ? 250 : undefined,
            autoScroll: rules.length > 10,
            rules: rules,
            symbolType: symbolType,
            selectOnClick: options.selectable,
            enableDD: false,
            listeners: {
                "ruleselected": function(cmp, rule) {
                    selectedRule = rule;
                    // enable the Remove, Edit and Duplicate buttons
                    var tbItems = rulesToolbar.items;
                    updateRuleRemoveButton();
                    tbItems.get(2).enable();
                    tbItems.get(3).enable();
                },
                "ruleunselected": function(cmp, rule) {
                    selectedRule = null;
                    // disable the Remove, Edit and Duplicate buttons
                    var tbItems = rulesToolbar.items;
                    tbItems.get(1).disable();
                    tbItems.get(2).disable();
                    tbItems.get(3).disable();
                },
                "afterlayout": function() {
                    // restore selection
                    //TODO QA: avoid accessing private properties/methods
                    if (selectedRule !== null &&
                            legend.selectedRule === null &&
                            legend.rules.indexOf(selectedRule) !== -1) {
                        legend.selectRuleEntry(selectedRule);
                    }
                },
                scope: this
            }
        });
        doLayout();
        return legend;
    }
	
	function normalizeStyle(style) {
		return style.replace(/<(ogc:)?Function(.*?)>.*?(<(ogc:)?PropertyName>.*?<\/(ogc:)?PropertyName>).*?<\/(ogc:)?Function>/g,'$3');
	}
	
	return {
		showLegend: function() {
			if(panel) {
				panel.destroy();
				panel = null;
			}
			rulesFieldSet = new Ext.form.FieldSet({
				itemId: "rulesfieldset",
				title: "Legend",
				style: "margin-bottom: 0;",
				items:[]
			});
			
			panel = new Ext.Container({
				title: 'Legend',
				renderTo: 'advancedLegend',
				width: 300,
				layout: "form",
				items: [rulesFieldSet]
			});
			
			parseStyle(false);
		},
		configureStyler: function(config) {
			SLD = normalizeStyle(config.style);
			layerName = config.layer;
			if(config.symbolType) {
				symbolType = config.symbolType;
			}
			if(config.attributes) {
				attributesInfo = config.attributes;
			}
		},
		showStyler: function() {
			if(panel) {
				panel.destroy();
				panel = null;
			}
			rulesFieldSet = new Ext.form.FieldSet({
				itemId: "rulesfieldset",
				title: "Rules",
				autoScroll: true,
				style: "margin-bottom: 0;",
				items:[]
			});
			rulesToolbar = new Ext.Toolbar({
				style: "border-width: 0 1px 1px 1px;",
				items: [
					{
						xtype: "button",
						iconCls: "add",
						text: "Add",
						tooltip: "Add a new rule",
						handler: addRule
					}, {
						xtype: "button",
						iconCls: "delete",
						text: "Remove",
						tooltip: "Delete the selected rule",
						handler: removeRule,
						disabled: true
					}, {
						xtype: "button",
						iconCls: "edit",
						text: "Edit",
						toolitp: "Edit the selected rule",
						handler: editRule,
						disabled: true
					}, {
						xtype: "button",
						iconCls: "duplicate",
						text: "Duplicate",
						tip: "Duplicate the selected rule",
						handler: duplicateRule,
						disabled: true
					}
				]
			});
		
			panel = new Ext.Panel({
				title: 'Advanced Styler',
				renderTo: 'advancedStyler',
				width: 300,
				height: 300,
				layout: "form",
				items: [
					{
						xtype:'container',
						autoScroll: true,
						height: 250,
						items: [
							rulesFieldSet
						]
					},
					rulesToolbar
				]
			});
			
			parseStyle(true);
		}
	};

	
})();

