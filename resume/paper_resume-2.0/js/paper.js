$(document).ready(function(){ 
	$('ul#info li:last').css('border-bottom', 'none');
	$('#portrait a:not(#image) img').css({'margin':'4px 2px 5px','vertical-align':'bottom'});

	function validateForm() {
		function checkEmail(email) {
			var pattern = new RegExp(/^(("[\w-\s]+")|([\w-]+(?:\.[\w-]+)*)|("[\w-\s]+")([\w-]+(?:\.[\w-]+)*))(@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$)|(@\[?((25[0-5]\.|2[0-4][0-9]\.|1[0-9]{2}\.|[0-9]{1,2}\.))((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\.){2}(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[0-9]{1,2})\]?$)/i);
			return pattern.test(email);	
		}

		var ok = 0, error;
		$('.req').each(function(i) {
			if(!$(this).val()) {
				/* error */
				$(this).css('border','2px solid #f2a5b1');
			} else {
				/* check mail */
				if($(this).hasClass('email') && !checkEmail($(this).val())) {
					$(this).css('border','2px solid #f2a5b1');
				} else { 
					/* clean */
					$(this).css('border','2px solid #dadada');
					ok = ok + 1;
				}
			}
			
			$(this).keypress(function() {
				$(this).css('border','2px solid #dadada');
			});
		});
		if (ok==4) return true;
	}
	
	$('#contact_form').submit(function() {
		var dataString = $(this).serialize();
		var status = '<span id="status"></span>';
		
		if (validateForm()) {
			$.ajax({
			  type: "POST",
			  url: "js/mail.php",
			  dataType: 'json',
			  data: dataString,
			  beforeSend: function() {
			   $('#status').remove();
				$('#submit').attr('disabled', 'disabled').after(status);
			   $('#status').removeClass().addClass('loading').text('Sending...');
			  },
			  success: function(data) {
			  	 if(data.error == 'true') {
			  	   $('#status').removeClass().addClass('error').text(data.msg);
			  	 } else {
			  	   $('.req').val('');
			  	   $('#status').removeClass().addClass('success').text(data.msg).delay(2000).fadeOut(function() { $.fancybox.close(); });
			  	 }
			  	 $('#submit').removeAttr('disabled');
			  },
			  error: function(data) {
			  	$('#status').text(data.msg);
			   
			  }
			});
		}
		
		return false;
	});

	/* Add Space to Section Titles */
	$('.section .title').each(function() {
		var string = $(this).text().split(""), result = "";
		
		for(var i=0;i<=string.length-1;i++) {
			(string[i] == ' ') ? result += '<br/><br/>' + string[i] : result += ' ' + string[i];
		}
		
		$(this).text('').append(result);
	});
		
	/* Fancybox */	
	$("a#image").fancybox();

	$(".contact").fancybox({
		'scrolling'		: 'no',
		'titleShow'		: false,
		'onClosed'		: function() {
		    $('input:text, textarea').val('').css('border', '2px solid #dadada');
		    $('#status').remove();
		}
	});

}); 