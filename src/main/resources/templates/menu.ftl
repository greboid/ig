<nav class="navbar navbar-expand navbar-light">
    <ul class="navbar-nav">
        <#list profiles as profile>
            <li class="nav-item">
                <a class="nav-link" href="/category/${profile}">${profile}</a>
            </li>
        </#list>
    </ul>
    <ul class="navbar-nav ml-auto">
        <#if username??>
            <li class="nav-item"><a class="nav-link" href="/admin">Admin</a></li>
            <li class="nav-item"><a class="nav-link" href="/logout">Logout</a></li>
        <#else>
            <li class="nav-item"><a class="nav-link" href="/login">Login</a></li>
        </#if>
    </ul>
</nav>
