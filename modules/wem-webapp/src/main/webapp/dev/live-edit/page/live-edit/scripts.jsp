<%
  if ( !"false".equals( request.getParameter( "edit" ) ) )
  {
%>
<!-- Namespace -->
<script type="text/javascript" charset="UTF-8" src="../app/js/namespace.js"></script>

<!-- Libs -->
<script type="text/javascript" charset="UTF-8" src="../../../admin/resources/lib/jquery/jquery-1.8.3.min.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/lib/jquery-ui-1.9.2.custom.min.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/lib/jquery.ui.touch-punch.min.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/lib/mutation_summary.js"></script>

<!-- Live Edit App -->
<script type="text/javascript" charset="UTF-8" src="../app/js/jquery.noconflict.js"></script>

<script type="text/javascript" charset="UTF-8" src="../app/js/Init.js"></script>

<script type="text/javascript" charset="UTF-8" src="../app/js/Util.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/js/MutationObserver.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/js/HtmlElementReplacer.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/js/Selection.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/js/DragDrop.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/js/PageLeave.js"></script>

<script type="text/javascript" charset="UTF-8" src="../app/js/model/Base.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/js/model/component/Base.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/js/model/component/Regions.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/js/model/component/Parts.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/js/model/component/Contents.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/js/model/component/Paragraphs.js"></script>

<script type="text/javascript" charset="UTF-8" src="../app/js/view/Base.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/js/view/Shader.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/js/view/Button.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/js/view/Cursor.js"></script>

<!--script type="text/javascript" charset="UTF-8" src="../app/js/view/hovermenu/HoverMenu.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/js/view/hovermenu/button/ParentButton.js"></script-->

<script type="text/javascript" charset="UTF-8" src="../app/js/view/Highlighter.js"></script>

<script type="text/javascript" charset="UTF-8" src="../app/js/view/ToolTip.js"></script>

<script type="text/javascript" charset="UTF-8" src="../app/js/view/componenttip/Tip.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/js/view/componenttip/menu/Menu.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/js/view/componenttip/menu/InsertButton.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/js/view/componenttip/menu/EditButton.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/js/view/componenttip/menu/ResetButton.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/js/view/componenttip/menu/EmptyButton.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/js/view/componenttip/menu/ViewButton.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/js/view/componenttip/menu/DragButton.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/js/view/componenttip/menu/SettingsButton.js"></script>
<script type="text/javascript" charset="UTF-8" src="../app/js/view/componenttip/menu/RemoveButton.js"></script>


<script type="text/javascript" charset="UTF-8" src="../app/js/view/componentbar/ComponentBar.js"></script>

<%
  }
%>
