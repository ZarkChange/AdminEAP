/**
 * Created by HANZO on 2016/6/17.
 */

/**
 * 待改
 * 
 * @param url
 * @param params
 * @param callback
 * @returns {*}
 */
function loadPage(url){
	$("#mainDiv").load(url,function(response,status,xhr){
		if(status=="success"){
			if(response){
				try{
					var result = jQuery.parseJSON(response);
					if(result.code==100){ 
						$("#mainDiv").html("");
						alert(result.data);
					}
				}catch(e){
					return response;
				}
			}
		}
	});
}
 
function ajaxPost(url, params, callback) {
	var result = null;

	$.ajax({
		type : 'post',
		async : false,
		url : url,
		data : params,
		dataType : 'json',
		success : function(data, status) {
			result = data;

			if (callback) {
				callback.call(this, data, status);
			}
		},
		error : function(err, err1, err2) {
			// console.error(JSON.stringify(err)+'<br/>err1:'+
			// JSON.stringify(err1)+'<br/>err2:'+JSON.stringify(err2));
			modals.error({
				text : JSON.stringify(err) + '<br/>err1:' + JSON.stringify(err1) + '<br/>err2:' + JSON.stringify(err2),
				large : true
			});
		}
	});

	return result;
}

/**
 * 获取初始化form表单组件
 * 
 * @param form_flag
 */
function initFormComponent(form_flag) {
	var form = $('#' + form_flag);
	form = form.length == 0 ? $('form[name="' + form_flag + '"]') : form;
	//给form表单增加BaseEntity中的属性
	if(form.find('[name="deleted"]').length==0){
		form.prepend("<input type='hidden' name='deleted'>");
	}
	if(form.find(':hidden[name="createDateTime"]').length==0){
		form.prepend('<input type="hidden" name="createDateTime" data-flag="date" data-format="yyyy-MM-dd HH:mm:ss">');
	} 
	if(form.find(':hidden[name="version"]').length==0){
		form.prepend("<input type='hidden' name='version'>");
	}
	if(form.find(':hidden[name="id"]').length==0){
		form.prepend("<input type='hidden' id='id' name='id'>");
	}
	// icheck
	if(form.find('[data-flag="icheck"]').length>0){
		form.find('[data-flag="icheck"]').iCheck({
			checkboxClass : 'icheckbox_square-green',
			radioClass : 'iradio_square-green'
		}).on('ifChanged', function(e) {
			// Get the field name
			var field = $(this).attr('name');
			form
			// Mark the field as not validated
			.bootstrapValidator('updateStatus', field, 'NOT_VALIDATED')
			// Validate field
			.bootstrapValidator('validateField', field);
		});
	}
	// datepicker
	if(form.find('[data-flag="datepicker"]').length>0){
		form.find('[data-flag="datepicker"]').datepicker({
			autoclose : true,
			format : 'yyyy-mm-dd',
			language : 'cn'
		}).on('change', function(e) {
			// Validate the date when user change it
			var field = $(this).attr('name');
			// Get the bootstrapValidator instance
			form.data('bootstrapValidator')
			// Mark the field as not validated, so it'll be re-validated when the
			// user change date
			.updateStatus(field, 'NOT_VALIDATED', null)
			// Validate the field
			.validateField(field);
		}).parent().css("padding-left","15px").css("padding-right","15px");
	}
}

/**
 * 获取表单信息
 * 
 * @param form_flag
 *            form id or name
 * @returns {{}}
 */
function getFormSimpleData(form_flag) {
	var datas = {};
   
	if (!form_flag)
		return datas;

	var form = $('#' + form_flag);
	form = form.length == 0 ? $('form[name="' + form_flag + '"]') : form;

	if (form.length == 0)
		return datas;

	var elems = form.find('input[name], select[name], textarea[name]');

	// 设置datas属性
	elems.each(function(ind, elem) {
		var el_name = elem.name;

		if (!el_name)
			return;

		var assembly = function(name) {
			var res = {}, sind = name.indexOf('.');
			res[sind > -1 ? name.substring(0, sind) : name] = sind > -1 ? assembly(name.substring(sind + 1)) : '';

			return res;
		};

		var ind = el_name.indexOf('.');
		datas[ind > -1 ? el_name.substring(0, ind) : el_name] = ind > -1 ? assembly(el_name.substring(ind + 1)) : '';
	});

	// 设置datas属性值
	elems.each(function(ind, elem) {
		var el_name = elem.name, is_radio = elem.type == 'radio', is_ckbox = elem.type == 'checkbox';

		if (!el_name || ((is_radio || is_ckbox) && !elem.checked))
			return;

		var old_val = eval('datas.' + el_name); // checkbox值用逗号分割
		eval('datas.' + el_name + '="' + (is_ckbox ? (old_val ? (old_val + ',') : '') : '') + elem.value + '"');
	});

	return datas;
}

/**
 * 初始化表单信息
 * 
 * @param form_flag
 *            form id or name
 * @param json_data {}
 */
function initFormData(form_flag, json_data) {
	if (!json_data || !form_flag)
		return;

	var form = $('#' + form_flag);
	form = form.length == 0 ? $('form[name="' + form_flag + '"]') : form;

	if (form.length == 0)
		return;

	form.find('input[name], select[name], textarea[name], label[name]').each(function(ind, elem) {
		var obj = $(elem), el_name = obj.attr('name'), value;

		try {
			value = eval('json_data.' + el_name);
		} catch (e) {
			value = null;
		}
        
		if (value != undefined && value != null && $.trim(value) != '') {
			var is_radio = elem.type == 'radio', is_ckbox = elem.type == 'checkbox';
            var is_date=$(elem).data("flag")=="datepicker"||$(elem).data("flag")=="date";
            var date_format=$(elem).data("format")||"yyyy-MM-dd";
            if(is_date) 
            	value=formatDate(value,date_format);
			if (is_radio) {  
				//icheck
				if($(elem).data("flag")=="icheck"){
					$(elem).iCheck( elem.value == value?'check':'uncheck');
					form.data('bootstrapValidator').updateStatus(el_name, 'NOT_VALIDATED', null);
				}else{
					//原生radio
					elem.checked = elem.value == value;
				}
			} else if (is_ckbox) {
				//icheck
				if($(elem).data("flag")=="icheck"){
					$(elem).iCheck($.inArray(elem.value, value.split(',')) > -1?'check':'uncheck');
					form.data('bootstrapValidator').updateStatus(el_name, 'NOT_VALIDATED', null);
				}else{
					//原生checkbox 
					elem.checked = $.inArray(elem.value, value.split(',')) > -1 ? true : false;
				}
			} else if (elem.tagName.toUpperCase() == 'LABEL') {
				elem.innerText = value;
			} else {
				elem.value = value;
			}
		}
	});
}

/**
 * 清空表单
 * 
 * @param form_flag
 *            form id or name
 */
function clearForm(form_flag) {
	if (form_flag) {
		var form = $('#' + form_flag);
		form = form.length == 0 ? $('form[name="' + form_flag + '"]') : form;

		form.find(':input[name]:not(:radio)').val('');
		form.find(':radio').attr('checked', false);
		form.find(':radio[data-flag]').iCheck('update');  
		form.find(':checkbox').attr('checked', false);
		form.find(':checkbox[data-flag]').iCheck('update');
		form.find('label[name]').text('');
	} else {
		$(':input[name]:not(:radio)').val('');
		$(':radio').removeAttr('checked');
		$(':radio[data-flag]').iCheck('update');
		$(':checkbox').removeAttr('checked');
		$(':checkbox[data-flag]').iCheck('update');
		$('label[name]').text('');
	}
}

/**
 * 格式化日期
 */
function formatDate(date, format) {
	if(!date)return date;
	date = (typeof date == "number") ? new Date(date) : date;
	return date.Format(format);
}

Date.prototype.Format = function(fmt) {
	var o = {
		"M+" : this.getMonth() + 1, // 月份
		"d+" : this.getDate(), // 日
		"H+" : this.getHours(), // 小时
		"m+" : this.getMinutes(), // 分
		"s+" : this.getSeconds(), // 秒
		"q+" : Math.floor((this.getMonth() + 3) / 3), // 季度
		"S" : this.getMilliseconds()
	// 毫秒
	};
	if (/(y+)/.test(fmt))
		fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
	for ( var k in o)
		if (new RegExp("(" + k + ")").test(fmt))
			fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
	return fmt;
}

/**
 * 将map类型[name,value]的数据转化为对象类型
 */
function getObjectFromMap(aData) {
	var map = {};
	for (var i = 0; i < aData.length; i++) {
		var item = aData[i];
		if (!map[item.name]) {
			map[item.name] = item.value;
		}
	}
	return map;
}

/**
 * 获取render,并转化为对象数组
 */
function getRenderObject(render) {
	var arr = render.split(",");
	var obj = new Object();
	for (var i = 0; i < arr.length; i++) {
		var strA = arr[i].split("=");
		obj[strA[0]] = strA[1];
	}
	if (!obj.type)
		obj.type = "eq";
	return obj;
}

/**
 * 获取下一个编码 000001，000001000006，6
 * 得到结果 000001000007
 */
function getNextCode(prefix,maxCode,length){
	if(maxCode==null){
		var str="";
		for(var i=0;i<length-1;i++){
			str+="0";
		}
		return prefix+str+1;
	}else{
		var str="";
		var sno = parseInt(maxCode.substring(prefix.length))+1;
		for(var i=0;i<length-sno.toString().length;i++){
			str+="0";
		}
		return prefix+str+sno;
	}
	
}

