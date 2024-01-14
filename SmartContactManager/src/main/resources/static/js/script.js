console.log("this is script file");
const toggleSidebar = () => {
    const $sidebar = $(".sidebar");
    const $content = $(".content");

    $sidebar.toggle();
    $content.css("margin-left", $sidebar.is(":visible") ? "20%" : "0%");
};

const search = () => {
   console.log("searching....");

    let query = $("#search-input").val();

    if(query == ""){
        $(".search-result").hide();
    }
    else{
        //search
        console.log(query);

        //sending request to the server

        let url=`http://localhost:8282/search/${query}`;
    
        fetch(url)
        .then((response) => {
            return response.json();
        })
        .then((data) => {
            //data.......
            console.log(data);

            let text = `<div class='list-group'>`;

            data.forEach((contact) => {
                text+= `<a href='/user/${contact.cId}/contact' class='list-group-item list-group-action'> ${contact.name} </a>`;
            });

            text+= `</div>`;

            $(".search-result").html(text);
            $(".search-result").show();
        });

    }
};



 