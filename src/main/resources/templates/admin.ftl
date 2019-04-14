<#include "/header.ftl">
<#include "/menu.ftl">
<div id="app" class="container-fluid">
    <div class="row">
        <div class="col"></div>
        <div class="col">
            <form class="form-inline" id="userForm" action="#" method="POST">
                <div class="form-group">
                    <label for="newUsername" class="col-sm-2">Users: </label>
                    <input class="form-control-inline target" id="newUsername" type="text" placeholder="New user"/>
                    <input class="btn btn-light" type="submit" value="Submit" id="addUser"/>
                </div>
            </form>
            <ul class="list-group sorted" id="userList">
            </ul>
        </div>
        <div class="col">
            <form class="form-inline" id="profileForm" action="#" method="POST">
                <div class="form-group">
                    <label for="newUsername" class="col-sm-2">Profiles: </label>
                    <input class="form-control-inline target" id="newProfile" type="text" placeholder="New profile"/>
                    <input class="btn btn-light" type="submit" value="Submit" id="addProfile"/>
                </div>
            </form>
            <ul class="list-group sorted" id="profileList">
            </ul>
        </div>
        <div class="col"></div>
    </div>
    <br><br>
    <div class="row">
        <div class="col"></div>
        <div class="col">
            <h4>User:</h4>
            <select id="userSelect" size="12" class="form-control">
            </select>
        </div>
        <div class="col">
            <h4>Profiles:</h4>
            <select id="profileSelect" multiple size="12" class="form-control">
            </select>
        </div>
        <div class="col"></div>
    </div>
    <br><br>
    <div class="row">
        <div class="col"></div>
        <div class="col centercontainer">
            <button id="save" class="btn btn-primary">Save</button>
        </div>
        <div class="col"></div>
    </div>
</div>
<script src="/js/jquery-3.3.1.min.js" charset="utf-8" nonce="uXhb3jHDu7bM9z4P"></script>
<script src="/js/admin.js" charset="utf-8" nonce="uXhb3jHDu7bM9z4P"></script>
<#include "/footer.ftl">
