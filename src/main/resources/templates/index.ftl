<#include "/header.ftl">
<div id="menu">
    <#list profiles as profile>
        <li><a href="/category/${profile}#">${profile}</a></li>
    </#list>
</div>
<div id="app" class="container">
    <#list images as image>
        <#include "/image.ftl">
    </#list>
</div>
<script src="/js/jquery-3.3.1.min.js" charset="utf-8" nonce="uXhb3jHDu7bM9z4P"></script>
<script src="/js/app.js" charset="utf-8" nonce="uXhb3jHDu7bM9z4P"></script>
<#include "/footer.ftl">
