$(function () {
    var $formLogin = $('#login-form');
    var $formLost = $('#lost-form');
    var $formRegister = $('#register-form');

    var $bottomLogin = $('#login-form-footer');
    var $bottomLost = $('#lost-form-footer');
    var $bottomRegister = $('#register-form-footer');

    var $divForms = $('#div-forms');
    var $modalAnimateTime = 300;
    var $msgAnimateTime = 150;
    var $msgShowTime = 2000;

    $('#login-modal').on('hidden.bs.modal', function (e) {
        cleanup();
    });

    $('#login-modal').on('shown.bs.modal', function (e) {
        $('#login_phone').focus();
    });

    $('#login_submit').click(function () {
        var $lg_phone = $('#login_phone').val();
        var $lg_password = $('#login_password').val();

        $.post("/login", {"user": $lg_phone, "password": $lg_password}, function (data, status) {
            $('#login-modal').modal('hide');
            showUserInfo(JSON.parse(data));
        }).fail(function (xhr, ajaxOptions, thrownError) {
            msgChange($('#div-login-msg'), $('#icon-login-msg'), $('#text-login-msg'), "error", "glyphicon-remove", "Ошибка входа");
        });
    });

    $('#lost_submit').click(function () {
        var $ls_phone = $('#lost_phone').val();
        $.post("/rememberPassword", {"phoneNumber": $ls_phone}, function (data, status) {
            msgChange($('#div-lost-msg'), $('#icon-lost-msg'), $('#text-lost-msg'), "success", "glyphicon-ok", "Пароль успешно отправлен");
            modalAnimate($formLost, $formLogin, $bottomLost, $bottomLogin, $('#login_password'));
            $("#login_phone").val($ls_phone);
            $("#text-login-msg").text("Пароль отправлен смс сообщением:");
        }).fail(function (xhr, ajaxOptions, thrownError) {
            msgChange($('#div-lost-msg'), $('#icon-lost-msg'), $('#text-lost-msg'), "error", "glyphicon-remove", "Ошибка отправки пароля");
        });
    });

    $('#register-submit').click(function () {
        var $rg_username = $('#register_username').val();
        var $rg_phone = $('#register_phone').val();
        $.post("/register", {"userName": $rg_username, "phoneNumber": $rg_phone}, function (data, status) {
            msgChange($('#div-register-msg'), $('#icon-register-msg'), $('#text-register-msg'), "success", "glyphicon-ok", "Вы успешно зарегистрированы");
            modalAnimate($formRegister, $formLogin, $bottomRegister, $bottomLogin, $('#login_password'));
            $("#login_phone").val($rg_phone);
            $("#text-login-msg").text("Введите пароль из смс сообщения:");
        }).fail(function (xhr, ajaxOptions, thrownError) {
            msgChange($('#div-register-msg'), $('#icon-register-msg'), $('#text-register-msg'), "error", "glyphicon-remove", "Ошибка регистрации пользователя");
        });
    });

    $("#login_phone").mask("+7 (999) 999-9999");
    $("#register_phone").mask("+7 (999) 999-9999");
    $("#lost_phone").mask("+7 (999) 999-9999");

    function showUserInfo(userInfo) {
        if (userInfo == null) {
            $("#nav-username").hide();
            $("#nav-logout").hide();
            $("#nav-login").show();
        } else {
            $("#nav-username").show();
            $("#nav-username").text(userInfo.name);
            $("#nav-logout").show();
            $("#nav-login").hide();
        }
    }

    $("#logout-link").on("click", function (event) {
        $.ajax({
            url: '/login',
            type: 'DELETE',
            success: function (result) {
                showUserInfo(null);
            }
        });
        event.preventDefault();
    });


    $('#login_register_btn').click(function () {
        modalAnimate($formLogin, $formRegister, $bottomLogin, $bottomRegister, $('#register_username'))
    });
    $('#register_login_btn').click(function () {
        modalAnimate($formRegister, $formLogin, $bottomRegister, $bottomLogin, $('#login_phone'));
    });
    $('#login_lost_btn').click(function () {
        modalAnimate($formLogin, $formLost, $bottomLogin, $bottomLost, $('#lost_phone'));
    });
    $('#lost_login_btn').click(function () {
        modalAnimate($formLost, $formLogin, $bottomLost, $bottomLogin, $('#login_phone'));
    });
    $('#lost_register_btn').click(function () {
        modalAnimate($formLost, $formRegister, $bottomLost, $bottomRegister, $('#register_username'));
    });
    $('#register_lost_btn').click(function () {
        modalAnimate($formRegister, $formLost, $bottomRegister, $bottomLost, $('#lost_phone'));
    });

    function modalAnimate($oldForm, $newForm, $oldBottom, $newBottom, $focus) {
        cleanup();
        var $oldH = $oldForm.height() + $oldBottom;
        var $newH = $newForm.height() + $newBottom;
        $divForms.css("height", $oldH);
        $oldBottom.hide();
        $newBottom.show();
        $oldForm.fadeToggle($modalAnimateTime, function () {
            $divForms.animate({height: $newH}, $modalAnimateTime, function () {
                $newForm.fadeToggle($modalAnimateTime);
                $focus.focus();
            });
        });
    }

    function msgFade($msgId, $msgText) {
        $msgId.fadeOut($msgAnimateTime, function () {
            $(this).text($msgText).fadeIn($msgAnimateTime);
        });
    }

    function msgChange($divTag, $iconTag, $textTag, $divClass, $iconClass, $msgText) {
        var $msgOld = $divTag.text();
        msgFade($textTag, $msgText);
        $divTag.addClass($divClass);
        $iconTag.removeClass("glyphicon-chevron-right");
        $iconTag.addClass($iconClass + " " + $divClass);
        setTimeout(function () {
            msgFade($textTag, $msgOld);
            $divTag.removeClass($divClass);
            $iconTag.addClass("glyphicon-chevron-right");
            $iconTag.removeClass($iconClass + " " + $divClass);
        }, $msgShowTime);
    }

    function cleanup() {
        cleanupLogin();
        cleanupLost();
        cleanupRegister();
    }

    function cleanupRegister() {
        $('#register_username').val("");
        $('#register_phone').val("");
        $("#text-register-msg").text("Регистрация нового пользователя.");
    }

    function cleanupLogin() {
        $('#login_phone').val("");
        $('#login_password').val("");
        $("#text-login-msg").text("Введите номер телефона и пароль.");
    }

    function cleanupLost() {
        $('#lost_phone').val("");
        $("#text-login-msg").text("Введите ваш номер телефона.");
    }
});