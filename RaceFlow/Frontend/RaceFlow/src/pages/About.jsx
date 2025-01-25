import Navbar from "../components/navbar";

function About() {
    return (
        <div>
            <Navbar />
            <div className="container">
                <div className="row">
                    <div className="col-lg-12">
                        <h1 className="mt-5">About RaceFlow</h1>
                        <p className="lead">This is a simple app to help you manage your racing events.</p>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default About;