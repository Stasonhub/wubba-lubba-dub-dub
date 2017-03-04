;(function () {

    'use strict';

    var contentWayPoint = function () {
        var i = 0;
        $('.animate-box').waypoint(function (direction) {

            if (direction === 'down' && !$(this.element).hasClass('animated-fast')) {

                i++;

                $(this.element).addClass('item-animate');
                setTimeout(function () {

                    $('body .animate-box.item-animate').each(function (k) {
                        var el = $(this);
                        setTimeout(function () {
                            var effect = el.data('animate-effect');
                            if (effect === 'fadeIn') {
                                el.addClass('fadeIn animated-fast');
                            } else if (effect === 'fadeInLeft') {
                                el.addClass('fadeInLeft animated-fast');
                            } else if (effect === 'fadeInRight') {
                                el.addClass('fadeInRight animated-fast');
                            } else {
                                el.addClass('fadeInUp animated-fast');
                            }

                            el.removeClass('item-animate');
                        }, k * 200, 'easeInOutExpo');
                    });

                }, 100);

            }

        }, {offset: '85%'});
    };


    $(function () {
        contentWayPoint();
    });


    var searchPriceFrom = $("#search-price-from");
    var searchPriceTo = $("#search-price-to");

    $("#search-price-range").slider({});
    $("#search-price-range").on("slide", function (slideEvt) {
        searchPriceFrom.text(slideEvt.value[0]);
        searchPriceTo.text(slideEvt.value[1]);
    });


    var loadMoreButton = $('#loadMore');
    var loadMoreButtonHider = function loadMoreButtonHider() {
        var count = $("#resultsBlock").children().last().attr("count");
        if (count < 15) {
            loadMoreButton.hide();
        }
    };

    loadMoreButtonHider();

    function get_last_timestamp() {
        return $("#resultsBlock").children().last().attr("timestamp");
    }


    var districtSelect = $('#district-select');
    var roomsButton1 = $('#rooms-btn1');
    var roomsButton2 = $('#rooms-btn2');
    var roomsButton3 = $('#rooms-btn3');
    var searchPriceRange = $('#search-price-range');

    var blurer = function () {
        $(this).blur();
    };
    roomsButton1.onfocus = blurer();
    roomsButton2.onfocus = blurer();
    roomsButton3.onfocus = blurer();

    var searchParamsGetter = function () {
        var rooms1 = roomsButton1.attr("aria-pressed")
        var rooms2 = roomsButton2.attr("aria-pressed")
        var rooms3 = roomsButton3.attr("aria-pressed")
        var districts = districtSelect.val() == undefined ? '' : districtSelect.val();
        var url = "?districts=" + districts;
        if (rooms1 != undefined) url += "&rooms1=" + rooms1;
        if (rooms2 != undefined) url += "&rooms2=" + rooms2;
        if (rooms3 != undefined) url += "&rooms3=" + rooms3;
        url += "&priceRange=" + searchPriceRange.slider('getValue');
        return url;
    };

    $('#search-button').on('click', function () {
        window.location.href = "/search/" + searchParamsGetter();
    });

    var initialSearchParams = searchParamsGetter();
    var isInSearchPage = $('#load-more-container').attr('search-page');
    loadMoreButton.on('click', function () {
        var url = isInSearchPage == 'true' ? '/search/loadMore' + initialSearchParams + '&' : '/loadMore?';
        url += 'timestampUntil=' + get_last_timestamp();
        $.get(url, function (html) {
            $("#resultsBlock").append(html);
            contentWayPoint();
            loadMoreButton.button('reset');
            loadMoreButtonHider();
        });
    });
}());