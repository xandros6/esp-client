var ESPStyler = (function() {
	return {
		showStyler: function() {
			var win = new Ext.Window({title: "Feature: ",
				layout: "fit",
				resizable: false,
				width: 220,
				x: 100, 
				y: 100,
				items: [
				    {
				    	hideBorders: true,
				    	border: false,
				    	autoHeight: true,
				    	items: [{
				    		xtype: "gx_legendpanel",
				    		title: "Rules used to render this feature:",
				    		bodyStyle: {paddingLeft: "5px"},
				    		symbolType: 'point',
				    		rules: [],
				    		clickableSymbol: true,
				    		listeners: {
				    			"symbolclick": function(panel, rule) {
			                        //this.showRule(this.currentLayer, rule, panel.symbolType);
			                    },scope: this}
				    		}, {
				    			xtype: "propertygrid",
				    			title: "Attributes of this feature:",
				    			height: 120,
				    			source: null,autoScroll: true,
				    			listeners: {
				    				"beforepropertychange": function() {
			                            return false;
				    				}
				    			}
				    		}
				    	]
				   }
				  ],
				  listeners: {
					  "move": function(cp, x, y) {
			                    
			          },
			          scope: this
			     }
			});
			win.show();
		}
	};

	
})();

