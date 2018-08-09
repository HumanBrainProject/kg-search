

export const generateKey = () => {
    let key = "";
    const chars = "ABCDEF0123456789";
    for (let i=0; i<4; i++) {
        if (key !== "")
          key += "-";
        for (let j=0; j<5; j++) {
            key += chars.charAt(Math.floor(Math.random() * chars.length));
        }
    }
    return key;
  };