import React from "react";
import PropTypes from "prop-types";


const Header = ({node, style}) => {
  const iconStyle = {marginRight: "5px"};
  return(
    <div style={style.base}>
      <div style={style.title}>
        {node.thumbnail ?
          <img height="14" width="12" src={node.thumbnail} alt={node.url} style={iconStyle} />:
          node.type === "file" ? <i className={"fa fa-file-o"} style={iconStyle}/>:null
        }
        {node.name}
      </div>
    </div>);
};

Header.propTypes = {
  style: PropTypes.object,
  node: PropTypes.object.isRequired
};

export default Header;