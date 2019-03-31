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
<#include "/footer.ftl">
