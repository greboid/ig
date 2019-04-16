<#include "/header.ftl">
<#include "/menu.ftl">
<div id="app" class="contentContainer">
    <#list images as image>
        <#include "/image.ftl">
    </#list>
</div>
<script src="/js/jquery-3.3.1.min.js"></script>
<script src="/js/popper-1.14.7.min.js"></script>
<script src="/js/bootstrap-4.3.1.min.js"></script>
<script src="/js/ekko-lightbox-5.3.0.min.js"></script>
<script src="/js/app.js"></script>
<#include "/footer.ftl">
