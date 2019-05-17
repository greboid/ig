<nav class="navbar navbar-expand navbar-light">
    <ul class="navbar-nav">
        <#list profiles as profile>
            <li class="nav-item">
                <a class="nav-link" href="/category/${profile}">${profile}</a>
            </li>
        </#list>
    </ul>
    <ul class="navbar-nav ml-auto">
        <li class="nav-item"><a class="nav-link" href="/admin">Login</a></li>
    </ul>
</nav>
