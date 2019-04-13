<#include "/header.ftl">
<div id="menu">
    <#list profiles as profile>
        <li><a href="/category/${profile}#">${profile}</a></li>
    </#list>
</div>
<div class="login-page">
    <div class="form">
        <form class="login-form" action="/login" method="post">
            <input name="username" id="username" type="text" placeholder="username"/>
            <input name="password" id="password" type="password" placeholder="password"/>
            <button>login</button>
        </form>
    </div>
</div>
<#include "/footer.ftl">
