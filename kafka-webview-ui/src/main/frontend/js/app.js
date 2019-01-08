/*****
* CONFIGURATION
*/
    //Main navigation
    $.navigation = $('nav > ul.nav');

  $.panelIconOpened = 'icon-arrow-up';
  $.panelIconClosed = 'icon-arrow-down';

  //Default colours
  $.brandPrimary =  '#20a8d8';
  $.brandSuccess =  '#4dbd74';
  $.brandInfo =     '#63c2de';
  $.brandWarning =  '#f8cb00';
  $.brandDanger =   '#f86c6b';

  $.grayDark =      '#2a2c36';
  $.gray =          '#55595c';
  $.grayLight =     '#818a91';
  $.grayLighter =   '#d1d4d7';
  $.grayLightest =  '#f8f9fa';

'use strict';

/****
* MAIN NAVIGATION
*/

$(document).ready(function($){

  // Add class .active to current link
  $.navigation.find('a').each(function(){

    var cUrl = String(window.location).split('?')[0];

    if (cUrl.substr(cUrl.length - 1) == '#') {
      cUrl = cUrl.slice(0,-1);
    }

    if ($($(this))[0].href==cUrl) {
      $(this).addClass('active');

      $(this).parents('ul').add(this).each(function(){
        $(this).parent().addClass('open');
      });
    }
  });

  // Dropdown Menu
  $.navigation.on('click', 'a', function(e){

    if ($.ajaxLoad) {
      e.preventDefault();
    }

    if ($(this).hasClass('nav-dropdown-toggle')) {
      $(this).parent().toggleClass('open');
      resizeBroadcast();
    }

  });

  function resizeBroadcast() {

    var timesRun = 0;
    var interval = setInterval(function(){
      timesRun += 1;
      if(timesRun === 5){
        clearInterval(interval);
      }
      window.dispatchEvent(new Event('resize'));
    }, 62.5);
  }

  /* ---------- Main Menu Open/Close, Min/Full ---------- */
  $('.sidebar-toggler').click(function(){
    $('body').toggleClass('sidebar-hidden');
    resizeBroadcast();
  });

  $('.sidebar-minimizer').click(function(){
    $('body').toggleClass('sidebar-minimized');
    resizeBroadcast();
  });

  $('.brand-minimizer').click(function(){
    $('body').toggleClass('brand-minimized');
  });

  $('.aside-menu-toggler').click(function(){
    $('body').toggleClass('aside-menu-hidden');
    resizeBroadcast();
  });

  $('.mobile-sidebar-toggler').click(function(){
    $('body').toggleClass('sidebar-mobile-show');
    resizeBroadcast();
  });

  $('.sidebar-close').click(function(){
    $('body').toggleClass('sidebar-opened').parent().toggleClass('sidebar-opened');
  });

  /* ---------- Disable moving to top ---------- */
  $('a[href="#"][data-top!=true]').click(function(e){
    e.preventDefault();
  });

});

/****
* CARDS ACTIONS
*/

$(document).on('click', '.card-actions a', function(e){
  e.preventDefault();

  if ($(this).hasClass('btn-close')) {
    $(this).parent().parent().parent().fadeOut();
  } else if ($(this).hasClass('btn-minimize')) {
    var $target = $(this).parent().parent().next('.card-block');
    if (!$(this).hasClass('collapsed')) {
      $('i',$(this)).removeClass($.panelIconOpened).addClass($.panelIconClosed);
    } else {
      $('i',$(this)).removeClass($.panelIconClosed).addClass($.panelIconOpened);
    }

  } else if ($(this).hasClass('btn-setting')) {
    $('#myModal').modal('show');
  }

});

function capitalizeFirstLetter(string) {
  return string.charAt(0).toUpperCase() + string.slice(1);
}

function init(url) {

  /* ---------- Tooltip ---------- */
  $('[rel="tooltip"],[data-rel="tooltip"]').tooltip({"placement":"bottom",delay: { show: 400, hide: 200 }});

  /* ---------- Popover ---------- */
  $('[rel="popover"],[data-rel="popover"],[data-toggle="popover"]').popover();
}

/**
 * Handles API Requests.
 */
var ApiClient = {
    // Returns the CSRF Token
    getCsrfToken: function() {
        return jQuery("meta[name='_csrf']").attr("content");
    },
    // Returns the CSRF Header name
    getCsrfHeader: function() {
        var headerName = jQuery("meta[name='_csrf_header']").attr("content");
        var headerValue = ApiClient.getCsrfToken();

        var headers = {};
        headers[headerName] = headerValue;
        return headers;
    },
    consume: function(viewId, params, successCallback) {
        jQuery.ajax({
            type: 'POST',
            url: '/api/consumer/view/' + viewId,
            dataType: 'json',
            contentType: 'application/json',
            data: JSON.stringify(params),
            headers: ApiClient.getCsrfHeader(),
            success: successCallback,
            error: ApiClient.defaultErrorHandler,
            beforeSend: function(xhr) {
                xhr.setRequestHeader('Content-type', 'application/json; charset=utf-8');
            }
        });
    },
    consumeNext: function(viewId, callback) {
        ApiClient.consume(viewId, {action:'next'}, callback);
    },
    consumePrevious: function(viewId, callback) {
        ApiClient.consume(viewId, {action:'previous'}, callback);
    },
    consumeTail: function(viewId, callback) {
        ApiClient.consume(viewId, {action:'tail'}, callback);
    },
    consumeHead: function(viewId, callback) {
        ApiClient.consume(viewId, {action:'head'}, callback);
    },
    seekTimestamp: function(viewId, unixTimestamp, callback) {
        jQuery.ajax({
            type: 'POST',
            url: '/api/consumer/view/' + viewId + '/timestamp/' + unixTimestamp,
            dataType: 'json',
            headers: ApiClient.getCsrfHeader(),
            success: callback,
            error: ApiClient.defaultErrorHandler
        });
    },
    setConsumerState: function(viewId, partitionOffsetJson, callback) {
        jQuery.ajax({
            type: 'POST',
            url: '/api/consumer/view/' + viewId + '/offsets',
            data: partitionOffsetJson,
            dataType: 'json',
            headers: ApiClient.getCsrfHeader(),
            success: callback,
            error: ApiClient.defaultErrorHandler,
            beforeSend: function(xhr) {
                xhr.setRequestHeader('Content-type', 'application/json; charset=utf-8');
            }
        });
    },
    /**
     * Retrieve which partitions are available for the given view.
     * @param viewId id of view
     * @param callback Call back to pass the results
     */
    getPartitionsForView: function(viewId, callback) {
        jQuery
            .getJSON('/api/view/' + viewId + '/partitions', '', callback)
            .fail(ApiClient.defaultErrorHandler);
    },

    // Retrieve cluster node info
    getClusterNodes: function(clusterId, callback) {
        jQuery
            .getJSON('/api/cluster/' + clusterId + '/nodes', '', callback)
            .fail(ApiClient.defaultErrorHandler);
    },
    getTopicDetails: function(clusterId, topic, callback) {
        jQuery
            .getJSON('/api/cluster/' + clusterId + '/topic/' + topic + '/details', '', callback)
            .fail(ApiClient.defaultErrorHandler);
    },
    getAllTopicsDetails: function(clusterId, callback) {
        jQuery
            .getJSON('/api/cluster/' + clusterId + '/topics/details', '', callback)
            .fail(ApiClient.defaultErrorHandler);
    },
    getTopics: function(clusterId, callback) {
        jQuery
            .getJSON('/api/cluster/' + clusterId + '/topics/list', '', callback)
            .fail(ApiClient.defaultErrorHandler);
    },
    getTopicConfig: function(clusterId, topic, callback) {
        jQuery
            .getJSON('/api/cluster/' + clusterId + '/topic/' + topic + '/config', '', callback)
            .fail(ApiClient.defaultErrorHandler);
    },
    getBrokerConfig: function(clusterId, brokerId, callback) {
        jQuery
            .getJSON('/api/cluster/' + clusterId + '/broker/' + brokerId + '/config', '', callback)
            .fail(ApiClient.defaultErrorHandler);
    },
    getAllConsumers: function(clusterId, callback) {
        jQuery
            .getJSON('/api/cluster/' + clusterId + '/consumers', '', callback)
            .fail(ApiClient.defaultErrorHandler);
    },
    getAllConsumersWithDetails: function(clusterId, callback) {
        jQuery
            .getJSON('/api/cluster/' + clusterId + '/consumersAndDetails', '', callback)
            .fail(ApiClient.defaultErrorHandler);
    },
    getConsumerDetails: function(clusterId, consumerGroupId, callback) {
        jQuery
            .getJSON('/api/cluster/' + clusterId + '/consumer/' + consumerGroupId + '/details', '', callback)
            .fail(ApiClient.defaultErrorHandler);
    },
    getConsumerOffsets: function(clusterId, consumerGroupId, callback) {
        jQuery
            .getJSON('/api/cluster/' + clusterId + '/consumer/' + consumerGroupId + '/offsets', '', callback)
            .fail(ApiClient.defaultErrorHandler);
    },
    getConsumerOffsetsWithTailPositions: function(clusterId, consumerGroupId, callback) {
        jQuery
            .getJSON('/api/cluster/' + clusterId + '/consumer/' + consumerGroupId + '/offsetsAndTailPositions', '', callback)
            .fail(ApiClient.defaultErrorHandler);
    },
    removeConsumer: function(clusterId, consumerId, callback) {
        var payload = JSON.stringify({
            clusterId: clusterId,
            consumerId: consumerId
        });
        jQuery.ajax({
            type: 'POST',
            url: '/api/cluster/' + clusterId + '/consumer/remove',
            data: payload,
            dataType: 'json',
            headers: ApiClient.getCsrfHeader(),
            success: callback,
            error: ApiClient.defaultErrorHandler,
            beforeSend: function(xhr) {
                xhr.setRequestHeader('Content-type', 'application/json; charset=utf-8');
            }
        });
    },
    createTopic: function(clusterId, name, partitions, replicas, callback) {
        var payload = JSON.stringify({
            name: name,
            partitions: partitions,
            replicas: replicas
        });
        jQuery.ajax({
            type: 'POST',
            url: '/api/cluster/' + clusterId + '/create/topic',
            data: payload,
            dataType: 'json',
            headers: ApiClient.getCsrfHeader(),
            success: callback,
            error: ApiClient.defaultErrorHandler,
            beforeSend: function(xhr) {
                xhr.setRequestHeader('Content-type', 'application/json; charset=utf-8');
            }
        });
    },
    modifyTopicConfig: function(clusterId, topic, configName, configValue, callback) {
        var payloadJson = {
            topic: topic,
            config: {}
        };
        payloadJson['config'][configName] = configValue;

        var payload = JSON.stringify(payloadJson);
        jQuery.ajax({
            type: 'POST',
            url: '/api/cluster/' + clusterId + '/modify/topic',
            data: payload,
            dataType: 'json',
            headers: ApiClient.getCsrfHeader(),
            success: callback,
            error: ApiClient.defaultErrorHandler,
            beforeSend: function(xhr) {
                xhr.setRequestHeader('Content-type', 'application/json; charset=utf-8');
            }
        });
    },
    defaultErrorHandler: function(jqXHR, textStatus, errorThrown) {
        // convert response to json
        var response = jQuery.parseJSON(jqXHR.responseText);
        UITools.showApiErrorWithStack(response);
    }
};

/**
 * Common UI Tooling.
 */
var UITools = {
    alertContainerId: "#AlertContainer",
    showAlert: function(message, timeoutInSecs) {
        var alertContainer = jQuery(UITools.alertContainerId);

        var alertElement = jQuery.parseHTML('<div class="alert alert-dismissable alert-danger"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button> <i class="icon-fire"></i> <span><strong>Error </strong>' + message + '</span></div>');
        jQuery(alertContainer)
            .append(alertElement);

        if (timeoutInSecs) {
            setTimeout(function() {
                jQuery(alertElement).alert('close');
            }, timeoutInSecs * 1000);
        }
    },
    showApiErrorWithStack: function(apiException) {
        var html = apiException.message
            + ' (<a data-toggle="collapse" href="#stackTrace" role="button" aria-expanded="false" aria-controls="stackTrace">show more</a>)<br/>'
            + '<div class="collapse" id="stackTrace"><ol>';


        var stackHtml = '';
        apiException.causes.forEach(function(entry, index) {
            stackHtml = stackHtml + '<li>' + entry.type + ' thrown at ' + entry.method + ' -> ' + entry.message + '</li>';
        });

        html = html + stackHtml + '</ul></div>';

        UITools.showAlert(html, null);
    },
    showSuccess: function(message, timeoutInSecs) {
        var alertContainer = jQuery(UITools.alertContainerId);

        var alertElement = jQuery.parseHTML('<div class="alert alert-dismissable alert-success"><button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button> <i class="icon-check"></i> <span><strong>Success </strong>' + message + '</span></div>');
        jQuery(alertContainer)
            .append(alertElement);

        if (timeoutInSecs) {
            setTimeout(function() {
                jQuery(alertElement).alert('close');
            }, timeoutInSecs * 1000);
        }
    }
};

/**
 * Common Date Tooling.
 */
var DateTools = {
    localTimezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
    displayTimestamp: function(timestampMs) {
        // Adjusts timestamp into local timezone and locate
        //return new Date(timestampMs).toLocaleString();
        var myDate = new Date(timestampMs);

        var year = myDate.getFullYear();
        var month = myDate.getMonth() + 1;
        var day = myDate.getDate();
        var hour = myDate.getHours();
        var min = myDate.getMinutes();
        var sec = myDate.getSeconds();

        month = (month < 10 ? "0" : "") + month;
        day = (day < 10 ? "0" : "") + day;
        hour = (hour < 10 ? "0" : "") + hour;
        min = (min < 10 ? "0" : "") + min;
        sec = (sec < 10 ? "0" : "") + sec;

        return year + '-' + month + '-' + day + ' ' + hour + ':' + min + ':' + sec + DateTools.formatTz(myDate);
    },
    formatTz: function(date) {
        var timezone_offset_min = date.getTimezoneOffset(),
            offset_hrs = parseInt(Math.abs(timezone_offset_min/60)),
            offset_min = Math.abs(timezone_offset_min%60),
            timezone_standard;

        if (offset_hrs < 10) {
            offset_hrs = '0' + offset_hrs;
        }

        if (offset_min < 10) {
            offset_min = '0' + offset_min;
        }

        // Add an opposite sign to the offset
        // If offset is 0, it means timezone is UTC
        if(timezone_offset_min < 0) {
            timezone_standard = '+' + offset_hrs + ':' + offset_min;
        } else if(timezone_offset_min > 0) {
            timezone_standard = '-' + offset_hrs + ':' + offset_min;
        } else if(timezone_offset_min == 0) {
            timezone_standard = 'Z';
        }

        return timezone_standard;
    }
};
