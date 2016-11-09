$(function () {
    var $formLogin = $('#login-form');
    var $formLost = $('#lost-form');
    var $formRegister = $('#register-form');
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

    $("form").submit(function () {
        switch (this.id) {
            case "login-form":
                var $lg_phone = $('#login_phone').val();
                var $lg_password = $('#login_password').val();

                $.post("/login", {"user": $lg_phone, "password": $lg_password}, function (data, status) {
                    $('#login-modal').modal('hide');
                    showUserInfo(JSON.parse(data));
                }).fail(function (xhr, ajaxOptions, thrownError) {
                    msgChange($('#div-login-msg'), $('#icon-login-msg'), $('#text-login-msg'), "error", "glyphicon-remove", "Ошибка входа");
                });
                return false;
                break;
            case "lost-form":
                var $ls_phone = $('#lost_phone').val();
                $.post("/rememberPassword", {"phoneNumber": $ls_phone}, function (data, status) {
                    msgChange($('#div-lost-msg'), $('#icon-lost-msg'), $('#text-lost-msg'), "success", "glyphicon-ok", "Пароль успешно отправлен");
                    modalAnimate($formLost, $formLogin, $('#login_password'));
                    $("#login_phone").val($ls_phone);
                    $("#text-login-msg").text("Пароль отправлен смс сообщением:");
                }).fail(function (xhr, ajaxOptions, thrownError) {
                    msgChange($('#div-lost-msg'), $('#icon-lost-msg'), $('#text-lost-msg'), "error", "glyphicon-remove", "Ошибка отправки пароля");
                });
                return false;
                break;
            case "register-form":
                var $rg_username = $('#register_username').val();
                var $rg_phone = $('#register_phone').val();
                $.post("/register", {"userName": $rg_username, "phoneNumber": $rg_phone}, function (data, status) {
                    msgChange($('#div-register-msg'), $('#icon-register-msg'), $('#text-register-msg'), "success", "glyphicon-ok", "Вы успешно зарегистрированы");
                    modalAnimate($formRegister, $formLogin, $('#login_password'));
                    $("#login_phone").val($rg_phone);
                    $("#text-login-msg").text("Введите пароль из смс сообщения:");
                }).fail(function (xhr, ajaxOptions, thrownError) {
                    msgChange($('#div-register-msg'), $('#icon-register-msg'), $('#text-register-msg'), "error", "glyphicon-remove", "Ошибка регистрации пользователя");
                });
                return false;
                break;
            default:
                return false;
        }
        return false;
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
        modalAnimate($formLogin, $formRegister, $('#register_username'))
    });
    $('#register_login_btn').click(function () {
        modalAnimate($formRegister, $formLogin, $('#login_phone'));
    });
    $('#login_lost_btn').click(function () {
        modalAnimate($formLogin, $formLost, $('#lost_phone'));
    });
    $('#lost_login_btn').click(function () {
        modalAnimate($formLost, $formLogin, $('#login_phone'));
    });
    $('#lost_register_btn').click(function () {
        modalAnimate($formLost, $formRegister, $('#register_username'));
    });
    $('#register_lost_btn').click(function () {
        modalAnimate($formRegister, $formLost, $('#lost_phone'));
    });

    function modalAnimate($oldForm, $newForm, $focus) {
        cleanup();
        var $oldH = $oldForm.height();
        var $newH = $newForm.height();
        $divForms.css("height", $oldH);
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