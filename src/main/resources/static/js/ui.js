function tools_dropdown_click()
{
$('#tools_dropdown').toggleClass('shown');
}
function attaches_dropdown_click()
{
$('#attaches_dropdown').toggleClass('shown');
}
function unfoldLeftPanelItemsListBlockFirst(){
	$('.items_list_block.top_block').toggleClass('unfolded');
}
function unfoldLeftPanelItemsListBlockSecond(){
		$('.items_list_block.middle_block').toggleClass('unfolded');
}