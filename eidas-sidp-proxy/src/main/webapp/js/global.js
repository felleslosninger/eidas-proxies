var idporten = {
	init: function() {
		if (window.isLoaded) {
			return;
		}
		window.isLoaded = true;

		var autoSubmitForm = document.getElementById("autosubmit");
		if (autoSubmitForm !== null) {
		// Auto-submit any forms with this ID
		autoSubmitForm.submit();
	}

	var autoFocus = document.getElementById("autofocus");
	if (autoFocus !== null) {
		// Auto-submit any forms with this ID
		try {
			autoFocus.focus();
		} catch (err) {
		}
	}

	//resizePopupBgHolder();
	placeCursorOnFirstElm();
	// Add viewport tag
	addViewportTag();
	this.events();

	resizeContainer();
	resizePopupBgHolder();

	if ($(".page-title").height() > 30) {
		$(".page-title").addClass("long");
	}
	
},
events: function() {
	$(".wrap.master input:checkbox").on("click", function() {
		if ($(this).is(':checked')) {
			$("input:checkbox").each(function() {
				$(this).attr('checked', true);
			});
		}
		else {
			$("input:checkbox").each(function() {
				$(this).attr('checked', false);
			});
		}
	});
	$(".wrap input:checkbox").on("click", function() {
		if (!$(this).hasClass("master")) {
			if (!$(this).is(':checked')) {
				$(".wrap.master input:checkbox").attr('checked', false);
			}
		}
	});
	$(".check-holder").hover(function() {
		$('label').addClass("hovered");
	}, function() {
		$('label').removeClass("hovered");
	});
	$("#nav li.sizes a").focusin(function() {
		$(this).find(".hint").show();
	});
	$("#nav li.sizes a").focusout(function() {
		$(this).find(".hint").hide();
	});
	$(".top-panel").on("click touchstart", "#mobile-menu", function(e) {
		e.preventDefault();
		e.stopPropagation();
		var container = $(this).closest('.container');
		if (container.hasClass("showMenu")) {
			container.removeClass("showMenu");
			$("#mobile-menu").removeClass("open");
			container.animate({
				left: "0"
			}, 500, function(){
				$(".mobile-menu").hide();
			});
		} 
		else {
			$("#mobile-menu").addClass("open");
			$(".mobile-menu").show();
			container.addClass("showMenu"); 
			container.animate({
				left: "-240px"
			}, 500, function(){});
		}
	});

	// Resize mobile menu
	$(window).resize(function() {
		resizeContainer();
		resizePopupBgHolder();
	});
}

};

function resizePopupBgHolder() {
	var popupExists = false;
	var popupBgHolder = document.getElementById("popup-bg-holder");
	if (popupBgHolder != null) {
		popupExists = true;
	// We are on a popup page
	var height = Math.max(0, getDocHeight());
	popupBgHolder.style.height = height + "px";
	// Attach escape listener
	var closeButton = document.getElementById("clickOnEsc");
	if (closeButton != null) {
		$("body").keyup(function(event) {
			if (event == null || event.keyCode != 27) {
				return;
			}
			if (closeButton.tagName.toLowerCase() == "a") {
				$(closeButton).bind('click', function() {
					window.location.href = this.href;
					return false;
				});
			}
			$("#clickOnEsc").click();
		});

	}
}
}

function resizeContainer() {
	
	var $mobileMenu = $(".mobile-menu");
	var $container = $(".container");
	//console.log($container.outerHeight(true), $mobileMenu.outerHeight(true));

	// Adjust heights
	if ($container.outerHeight(true) < $mobileMenu.outerHeight(true)) {
		$container.height($mobileMenu.outerHeight() + 0);	
	} else {	
		$mobileMenu.height($container.outerHeight() + 0);
		
		// Close opened mobile menu when resizing from mobile to desktop view
		if ($container.hasClass("showMenu") && $('.container .hidden-phone').is(':visible')) {  
			
			$("#mobile-menu")
				.removeClass("open")
			;
			$('.mobile-menu')
				.hide()
			;
			$container
				.removeClass("showMenu")
				.css('left', 0)
			;
			/*
			$("#mobile-menu").removeClass("open");
			$container.removeClass("showMenu");
			$container.animate({
				left: "0"
			}, 500, function(){
				$(".mobile-menu").hide();
			});
			*/
		}
	}
}

$(document).ready(function() {
	idporten.init();
});

function getDocHeight() {

	var D = document;
	return Math.max(
		Math.max(D.body.scrollHeight, D.documentElement.scrollHeight),
		Math.max(D.body.offsetHeight, D.documentElement.offsetHeight),
		Math.max(D.body.clientHeight, D.documentElement.clientHeight)
		);

}

function checkEmail(emailID, text) {
	var email = document.getElementById(emailID);
	var filter = /^([a-zA-Z0-9_.-])+@(([a-zA-Z0-9-])+.)+([a-zA-Z0-9]{2,4})+$/;
	if (!filter.test(email.value)) {
		alert(text);
		email.focus();
		return false;
	}
	return true;
}

function consentCheck(alertUser) {
	var checkbox = document.getElementById("consent");
	var errorMsg = $(".box-error2");
	if (!checkbox.checked) {
		if (alertUser) {
			if (errorMsg.hasClass("hide")) {
				errorMsg.removeClass("hide");
			}
		}
		return false;
	} else {
		if (!errorMsg.hasClass("hide")) {
			errorMsg.addClass("hide");
		}
		return true;
	}
}

function setCookie(name, value, days) {
	if (days) {
		var date = new Date();
		date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
		var expires = "; expires=" + date.toGMTString();
	}
	else
		expires = "";
	document.cookie = name + "=" + value + expires + "; path=/";
}

function getCookie(name) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for (var i = 0; i < ca.length; i++) {
		var c = ca[i];
		while (c.charAt(0) == ' ')
			c = c.substring(1, c.length);
		if (c.indexOf(nameEQ) == 0)
			return c.substring(nameEQ.length, c.length);
	}
	return "";
}

function placeCursorOnFirstElm() {
	var frms = document.forms;
	var frmCount = frms.length;

	for (var i = 0; i < frmCount; i++) {
		var frm = frms[i];
		var sz = frm.elements.length;

		for (var j = 0; j < sz; j++) {
			var elm = frm.elements[j];
			if (elm.type == "text" || elm.type == "password" || elm.type == "checkbox" || elm.type == "radio"||elm.type == "tel"||elm.type =="email") {
				elm.focus();
				return;
			}
		}
	}
}

// Make sure forms are only submitted once
function ensureSingleSubmit() {
	if (window.formHasBeenSubmitted) {
		return false;
	}
	window.formHasBeenSubmitted = true;
	return true;
}

function addViewportTag() {
	if (window.matchMedia) {
		var mq = window.matchMedia("(max-width: 480px)");
		mq.addListener(WidthChange);
		WidthChange(mq);
	}

	function setViewport(attr) {
		if (document.querySelector) {
			viewport = document.querySelector("meta[name=viewport]");
			viewport.setAttribute('content', attr);

		}
	}

	// media query change
	function WidthChange(mq) {
		if (mq.matches) {
			setViewport('width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0');
		} else {
			setViewport('width=device-width, initial-scale=1.0, maximum-scale=10.0');
		}
	}
}

